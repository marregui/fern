package com.fern.seq;

import com.fern.util.Util;

import static com.fern.util.Util.str;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class Map extends ABaseHashed {

    public static IHashed neu(final Object... keyValPairs) {
        if (keyValPairs == null) {
            throw new NullPointerException();
        }
        return new Map(keyValPairs);
    }

    private static class Entry extends ABaseSeq implements IHashedEntry {
        private final Object key;
        private final Object value;

        Entry(final Object key, final Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Object first() {
            return key;
        }

        @Override
        public Object last() {
            return value;
        }

        @Override
        public String toString() {
            return Util.str("[%s, %s]", key, value);
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof ISeq that) {
                return (key == that.first() || key != null && key.equals(that.first())) &&
                        (value  == that.last() || value != null && value.equals(that.last()));
            }
            return false;
        }

        @Override
        public int hashCode() {
            int seed = 31;
            int result = 17;
            result = result * seed + (null != key ? key.hashCode() : 0);
            result = result * seed + (null != value ? value.hashCode() : 0);
            return result;
        }

        @Override
        public Iterator<Object> iterator() {
            return new Iterator<>() {
                private int idx = 0;

                @Override
                public final boolean hasNext() {
                    return idx < 2;
                }

                @Override
                public Object next() {
                    if (hasNext()) {
                        switch (this.idx++) {
                            case 0:
                                return key;
                            case 1:
                                return value;
                        }
                    }
                    throw new IndexOutOfBoundsException();
                }
            };
        }
    }

    private final AtomicReference<ISeq> snapshotKeys;
    private final AtomicReference<ISeq> snapshotVals;

    private Map(Object... keyValPairs) {
        if (keyValPairs.length % 2 != 0) {
            throw new IllegalArgumentException("even number of args required: (key, val)*");
        }
        snapshotKeys = new AtomicReference<>();
        snapshotVals = new AtomicReference<>();
        for (int pairIdx = 0; pairIdx < keyValPairs.length; pairIdx += 2) {
            store(keyValPairs[pairIdx], keyValPairs[pairIdx + 1]);
        }
    }

    @Override
    void storeInBucket(IHashedEntry[] bucket, Object key, Object val) {
        for (int i = 0; i < bucket.length; i++) {
            boolean newEntry = (bucket[i] == null);
            if (newEntry || bucket[i].first().equals(key)) {
                bucket[i] = new Entry(key, val);
                if (newEntry) {
                    size.incrementAndGet();
                    snapshotKeys.set(null);
                }
                snapshotVals.set(null);
                snapshotEntries.set(null);
                quickToArray.set(null);
                break;
            }
        }
    }

    @Override
    public Object[] toArray() {
        Object[] array = quickToArray.get();
        if (array != null) {
            return array;
        }
        ISeq items = items();
        array = new Object[items.size() * 2];
        for (int i = 0, j = 0; i < array.length; i += 2, j++) {
            ISeq nth = (ISeq) items.nth(j);
            array[i] = nth.first();
            array[i + 1] = nth.last();
        }
        quickToArray.compareAndSet(null, array);
        return quickToArray.get();
    }

    @Override
    public ISeq keys() {
        return accessSnapshot(Collect.KEYS, snapshotKeys);
    }

    @Override
    public ISeq values() {
        return accessSnapshot(Collect.VALS, snapshotVals);
    }

    @Override
    public IHashed assoc(Object key, Object val) {
        if (findKey(key) == null) {
            store(key, val);
            return this;
        }
        ISeq entries = items();
        IHashed newMap = new Map(key, val);
        for (int i = 0; i < entries.size(); i++) {
            final ISeq entry = (ISeq) entries.nth(i);
            if (false == entry.first().equals(key)) {
                newMap.cons(entry);
            }
        }
        return newMap;
    }

    @Override
    public ISeq cons(final Object e) {
        if (e instanceof ISeq entry) {
            if (entry.size() == 2) {
                return assoc(entry.first(), entry.last());
            }
        }
        throw new IllegalArgumentException(str("expected |ISeq| == 2, got: %s", e));
    }
}