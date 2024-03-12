package com.fern.seq;

import com.fern.util.Util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

abstract class ABaseHashed extends ABaseSeq implements IHashed {
    static final int NUM_BUCKETS = 17;
    static final int BUCKET_SIZE = 23;
    static final int BUCKET_GROWTH_FACTOR = 2;

    interface IHashedEntry extends ISeq, Comparable<IHashedEntry> {
        @Override
        default int compareTo(IHashedEntry that) {
            return first().toString().compareTo(that.first().toString());
        }
    }

    private final IHashedEntry[][] buckets;
    private final ReentrantLock bucketsTopLock;
    private final ReentrantLock[] bucketLock;
    final AtomicInteger size;
    final AtomicReference<ISeq> snapshotEntries;
    final AtomicReference<Object[]> quickToArray;

    ABaseHashed() {
        size = new AtomicInteger();
        buckets = new IHashedEntry[NUM_BUCKETS][];
        bucketsTopLock = new ReentrantLock();
        bucketLock = new ReentrantLock[NUM_BUCKETS];
        for (int i = 0; i < NUM_BUCKETS; i++) {
            bucketLock[i] = new ReentrantLock();
        }
        snapshotEntries = new AtomicReference<>();
        quickToArray = new AtomicReference<>();
    }

    @Override
    public Object invoke(Object... args) {
        if (args == null || args.length == 0 || args.length > 1) {
            throw new IllegalArgumentException("only one arg is allowed, a key, to return its associated value");
        }
        return get(args[0]);
    }

    @Override
    public boolean contains(Object key) {
        return key != null && findKey(key) != null;
    }

    @Override
    public ISeq items() {
        return accessSnapshot(Collect.ENTRIES, snapshotEntries);
    }

    @Override
    public ISeq keys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISeq values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IHashed assoc(Object key, Object val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IHashed assoc(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(Object key) {
        IHashedEntry keyVal = findKey(key);
        return keyVal != null ? keyVal.last() : null;
    }

    @Override
    public Object first() {
        return items().first();
    }

    @Override
    public Object last() {
        return items().last();
    }

    @Override
    public Object nth(int n) {
        return items().nth(n);
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return size.get() == 0;
    }

    @Override
    public ISeq rest() {
        return items().rest();
    }

    @Override
    public ISeq cone(Object e) {
        return cons(e);
    }

    @Override
    public Iterator<Object> iterator() {
        return items().iterator();
    }

    @Override
    public ISeq sorted() {
        return sorted(DEFAULT_COMPARATOR);
    }

    @Override
    public ISeq sorted(Comparator<Object> comparator) {
        return items().sorted(comparator);
    }

    int bucketIdx(Object key) {
        return Math.abs(key.hashCode()) % buckets.length;
    }

    void store(Object key, Object val) {
        int bucketIdx = bucketIdx(key);
        IHashedEntry[] bucket;
        bucketsTopLock.lock();
        try {
            bucket = buckets[bucketIdx];
            if (bucket == null) {
                buckets[bucketIdx] = (bucket = new IHashedEntry[BUCKET_SIZE]);
            } else if (bucket[bucket.length - 1] != null) {
                IHashedEntry[] newBucket = new IHashedEntry[bucket.length * BUCKET_GROWTH_FACTOR];
                System.arraycopy(bucket, 0, newBucket, 0, bucket.length);
                buckets[bucketIdx] = (bucket = newBucket);
            }
            bucketLock[bucketIdx].lock();
        } finally {
            bucketsTopLock.unlock();
        }

        try {
            storeInBucket(bucket, key, val);
        } finally {
            bucketLock[bucketIdx].unlock();
        }
    }

    abstract void storeInBucket(IHashedEntry[] bucket, Object key, Object val);

    @Override
    public String toString() {
        StringBuilder sb = Util.THR_SB.get();
        sb.append("{");
        ISeq entries = items();
        for (int i = 0; i < entries.size(); i++) {
            sb.append(entries.nth(i)).append(TO_STR_SEP);
        }
        if (entries.size() > 0) {
            sb.setLength(sb.length() - TO_STR_SEP.length());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof IHashed that) {
            if (size.get() != that.size()) {
                return false;
            }
            return items().sorted().equals(that.items().sorted());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 1;
        ISeq entries = items();
        for (int i = 0; i < entries.size(); i++) {
            result = 31 * result + entries.nth(i).hashCode();
        }
        return result;
    }

    IHashedEntry findKey(Object key) {
        int bucketIdx = bucketIdx(key);
        IHashedEntry[] bucket;
        bucketsTopLock.lock();
        try {
            bucket = buckets[bucketIdx];
            if (bucket == null) {
                return null;
            }
            bucketLock[bucketIdx].lock();
        } finally {
            bucketsTopLock.unlock();
        }
        try {
            for (int i = 0; i < bucket.length; i++) {
                IHashedEntry entry = bucket[i];
                if (entry == null) {
                    return null;
                }
                if (entry.first().equals(key)) {
                    return entry;
                }
            }
            return null;
        } finally {
            bucketLock[bucketIdx].unlock();
        }
    }

    ISeq accessSnapshot(Collect target, AtomicReference<ISeq> current) {
        ISeq snapshot = current.get();
        if (snapshot == null) {
            bucketsTopLock.lock();
            acquireAllBucketLocks();
            try {
                snapshot = target.asList(buckets, size.get());
            } finally {
                releseAllBucketLocks();
                bucketsTopLock.unlock();
            }
            current.set(snapshot);
        }
        return snapshot;
    }

    /**
     * Goes through the buckets and takes either the keys, the values, or both (entries)
     * to return them as an Array. External locking on the buckets is assumed
     */
    enum Collect {
        KEYS, VALS, ENTRIES;

        ISeq asList(IHashedEntry[][] buckets, int size) {
            Object[] collected = new Object[size];
            int collectedIdx = 0;
            for (int i = 0; i < buckets.length; i++) {
                IHashedEntry[] bucket = buckets[i];
                if (bucket != null) {
                    for (int j = 0; j < bucket.length; j++) {
                        IHashedEntry entry = bucket[j];
                        if (entry == null) {
                            break;
                        }
                        collected[collectedIdx++] = take(entry);
                    }
                }
            }
            return new List(collected);
        }

        private Object take(IHashedEntry entry) {
            Object item = null;
            switch (this) {
                case KEYS:
                    item = entry.first();
                    break;
                case VALS:
                    item = entry.last();
                    break;
                case ENTRIES:
                    item = entry;
                    break;
            }
            return item;
        }
    }

    private void acquireAllBucketLocks() {
        // used only by the snapshot functionality
        for (int i = 0; i < bucketLock.length; i++) {
            bucketLock[i].lock();
        }
    }

    private void releseAllBucketLocks() {
        // used only by the snapshot functionality
        for (int i = 0; i < bucketLock.length; i++) {
            bucketLock[i].unlock();
        }
    }
}