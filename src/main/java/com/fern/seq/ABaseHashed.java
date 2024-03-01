package com.fern.seq;

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
    default int compareTo(final IHashedEntry that) {
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
    this.size = new AtomicInteger();
    this.buckets = new IHashedEntry[NUM_BUCKETS][];
    this.bucketsTopLock = new ReentrantLock();
    this.bucketLock = new ReentrantLock[NUM_BUCKETS];
    for (int i = 0; i < NUM_BUCKETS; i++) {
      this.bucketLock[i] = new ReentrantLock();
    }
    this.snapshotEntries = new AtomicReference<>();
    this.quickToArray = new AtomicReference<>();
  }
  
  @Override
  public Object invoke(Object... args){
    if (args == null || args.length == 0 || args.length > 1) {
      throw new IllegalArgumentException("only one arg is allowed, a key, to return its associated value");
    }
    return get(args[0]);
  }
  
  @Override
  public boolean contains(final Object key) {
    return key != null && findKey(key) != null;
  }

  @Override
  public ISeq items() {
    return accessSnapshot(Collect.ENTRIES, this.snapshotEntries);
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
  public Object get(final Object key) {
    final IHashedEntry keyVal = findKey(key);
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
  public Object nth(final int n) {
    return items().nth(n);
  }
  
  @Override
  public int size() {
    return this.size.get();
  }
  
  @Override
  public boolean isEmpty() {
    return this.size.get() == 0;
  }

  @Override
  public ISeq rest() {
    return items().rest();
  }

  @Override
  public ISeq cone(final Object e) {
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

  final int bucketIdx(Object key) {
    return Math.abs(key.hashCode()) % this.buckets.length;
  }

  final void store(final Object key, final Object val) {
    final int bucketIdx = bucketIdx(key);
    IHashedEntry[] bucket = null;
    this.bucketsTopLock.lock();
    try {
      bucket = this.buckets[bucketIdx];
      if (bucket == null) {
        this.buckets[bucketIdx] = (bucket = new IHashedEntry[BUCKET_SIZE]);
      }
      else if (bucket[bucket.length - 1] != null) {
        final IHashedEntry[] newBucket = new IHashedEntry[bucket.length * BUCKET_GROWTH_FACTOR];
        System.arraycopy(bucket, 0, newBucket, 0, bucket.length);
        this.buckets[bucketIdx] = (bucket = newBucket);
      }
      this.bucketLock[bucketIdx].lock();
    }
    finally {
      this.bucketsTopLock.unlock();
    }

    try {
      storeInBucket(bucket, key, val);
    }
    finally {
      this.bucketLock[bucketIdx].unlock();
    }
  }

  abstract void storeInBucket(final IHashedEntry[] bucket, final Object key, final Object val);

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("{");
    final ISeq entries = items();
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
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || false == o instanceof IHashed) {
      return false;
    }
    final IHashed that = (IHashed) o;
    if (this.size.get() != that.size()) {
      return false;
    }
    return items().sorted().equals(that.items().sorted());
  }

  @Override
  public int hashCode() {
    int result = 1;
    final ISeq entries = items();
    for (int i = 0; i < entries.size(); i++) {
      result = 31 * result + entries.nth(i).hashCode();
    }
    return result;
  }

  final IHashedEntry findKey(final Object key) {
    final int bucketIdx = bucketIdx(key);
    IHashedEntry[] bucket = null;
    this.bucketsTopLock.lock();
    try {
      bucket = this.buckets[bucketIdx];
      if (bucket == null) {
        return null;
      }
      this.bucketLock[bucketIdx].lock();
    }
    finally {
      this.bucketsTopLock.unlock();
    }
    try {
      for (int i = 0; i < bucket.length; i++) {
        final IHashedEntry entry = bucket[i];
        if (entry == null) {
          return null;
        }
        if (entry.first().equals(key)) {
          return entry;
        }
      }
      return null;
    }
    finally {
      this.bucketLock[bucketIdx].unlock();
    }
  }

  final ISeq accessSnapshot(final Collect target, final AtomicReference<ISeq> current) {
    ISeq snapshot = current.get();
    if (snapshot == null) {
      this.bucketsTopLock.lock();
      acquireAllBucketLocks();
      try {
        snapshot = target.asList(this.buckets, this.size.get());
      }
      finally {
        releseAllBucketLocks();
        this.bucketsTopLock.unlock();
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

    ISeq asList(final IHashedEntry[][] buckets, final int size) {
      final Object[] collected = new Object[size];
      int collectedIdx = 0;
      for (int i = 0; i < buckets.length; i++) {
        final IHashedEntry[] bucket = buckets[i];
        if (bucket != null) {
          for (int j = 0; j < bucket.length; j++) {
            final IHashedEntry entry = bucket[j];
            if (entry == null) {
              break;
            }
            collected[collectedIdx++] = take(entry);
          }
        }
      }
      return new List(collected);
    }

    private final Object take(final IHashedEntry entry) {
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

  private final void acquireAllBucketLocks() {
    // used only by the snapshot functionality
    for (int i = 0; i < this.bucketLock.length; i++) {
      this.bucketLock[i].lock();
    }
  }

  private final void releseAllBucketLocks() {
    // used only by the snapshot functionality
    for (int i = 0; i < this.bucketLock.length; i++) {
      this.bucketLock[i].unlock();
    }
  }
}