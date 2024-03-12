package com.fern.seq;

import com.fern.util.Util;

import java.util.Iterator;

public class Set extends ABaseHashed {

    public static IHashed neu(Object... entries) {
        if (entries == null) {
            throw new NullPointerException();
        }
        return new Set(entries);
    }

    private static class Entry extends ABaseSeq implements IHashedEntry {
        private final Object key;

        Entry(final Object key) {
            this.key = key;
        }

        @Override
        public Object first() {
            return key;
        }

        @Override
        public Object last() {
            return key;
        }

        @Override
        public String toString() {
            return Util.str("[%s]", key);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof ISeq that) {
                return (key == that.first() || key != null && key.equals(that.first()));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return null != key ? key.hashCode() : 0;
        }

        @Override
        public Iterator<Object> iterator() {
            return new Iterator<>() {
                private int idx = 0;

                @Override
                public boolean hasNext() {
                    return idx < 1;
                }

                @Override
                public Object next() {
                    if (hasNext()) {
                        idx++;
                        return key;
                    }
                    throw new IndexOutOfBoundsException();
                }
            };
        }
    }

    private Set(Object... entries) {
        for (int i = 0; i < entries.length; i++) {
            store(entries[i], null);
        }
    }

    @Override
    void storeInBucket(IHashedEntry[] bucket, Object key, Object val) {
        for (int i = 0; i < bucket.length; i++) {
            if (bucket[i] == null) {
                bucket[i] = new Entry(key);
                size.incrementAndGet();
                snapshotEntries.set(null);
                quickToArray.set(null);
                break;
            } else if (bucket[i].first().equals(key)) {
                break;
            }
        }
    }

    @Override
    public ISeq keys() {
        return items();
    }

    @Override
    public ISeq values() {
        return items();
    }

    @Override
    public Object[] toArray() {
        Object[] array = this.quickToArray.get();
        if (array != null) {
            return array;
        }
        ISeq items = items();
        array = new Object[items.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = ((ISeq) items.nth(i)).first();
        }
        quickToArray.compareAndSet(null, array);
        return this.quickToArray.get();
    }

    @Override
    public IHashed assoc(final Object entry) {
        if (findKey(entry) == null) {
            store(entry, null);
            return this;
        }
        ISeq entries = items();
        IHashed newSet = new Set(entry);
        for (int i = 0; i < entries.size(); i++) {
            ISeq nthEntry = (ISeq) entries.nth(i);
            if (false == nthEntry.first().equals(entry)) {
                newSet.cons(nthEntry.first());
            }
        }
        return newSet;
    }

    @Override
    public ISeq cons(final Object entry) {
        return assoc(entry);
    }
}