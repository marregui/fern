package com.fern.seq;

import static com.fern.util.Tools.str;

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
      return this.key;
	}
	
	@Override
	public Object last() {
      return this.value;
	}
	
	@Override
	public String toString() {
	  return String.format("[%s, %s]", this.key, this.value);
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
	  if (null == o || false == (o instanceof ISeq)) {
	    return false;
	  }
	  final ISeq that = (ISeq) o;
	  return (this.key == that.first() || this.key != null && this.key.equals(that.first())) &&
	      (this.value  == that.last() || this.value != null && this.value.equals(that.last()));
	}
	
	@Override
	public int hashCode() {
	  int seed = 31;
	  int result = 17;
	  result = result * seed + (null != this.key? this.key.hashCode() : 0);
	  result = result * seed + (null != this.value? this.value.hashCode() : 0);
	  return result;
	}
	
	@Override
	  public Iterator<Object> iterator() {
	    return new Iterator<Object>() {
	      private int idx = 0;

	      @Override
	      public final boolean hasNext() {
	        return this.idx < 2;
	      }

	      @Override
	      public Object next() {
	        if (hasNext()) {
	          switch (this.idx++) {
	            case 0: return key;
	            case 1: return value;
	          }
	        }
	        throw new IndexOutOfBoundsException();
	      }
	    };
	  }
  }

  private final AtomicReference<ISeq> snapshotKeys;
  private final AtomicReference<ISeq> snapshotVals;

  private Map(final Object... keyValPairs) {
    if (keyValPairs.length % 2 != 0) {
      throw new IllegalArgumentException("even number of args required: (key, val)*");
    }
    this.snapshotKeys = new AtomicReference<>();
    this.snapshotVals = new AtomicReference<>();
    for (int pairIdx = 0; pairIdx < keyValPairs.length; pairIdx += 2) {
      store(keyValPairs[pairIdx], keyValPairs[pairIdx + 1]);
    }
  }
  
  @Override
  void storeInBucket(IHashedEntry[] bucket, Object key, Object val) {
    for (int i = 0; i < bucket.length; i++) {
      final boolean newEntry = (bucket[i] == null);
      if (newEntry || bucket[i].first().equals(key)) {
        bucket[i] = new Entry(key, val);
        if (newEntry) {
          this.size.incrementAndGet();
          this.snapshotKeys.set(null);
        }
        this.snapshotVals.set(null);
        this.snapshotEntries.set(null);
        this.quickToArray.set(null);
        break;
      }
    }
  }
  
  @Override
  public Object [] toArray() {
    Object [] array = this.quickToArray.get();
    if (array == null) {
      synchronized (this.quickToArray) {
        array = this.quickToArray.get();
        if (array == null) {
          final ISeq items = items();
          array = new Object[items.size() * 2];
          for (int i=0, j=0; i < array.length; i+=2, j++) {
            final ISeq nth = (ISeq) items.nth(j);
            array[i] = nth.first();
            array[i+1] = nth.last();
          }
          this.quickToArray.set(array);
        }
      }
    }
    return this.quickToArray.get();
  }

  @Override
  public ISeq keys() {
    return accessSnapshot(Collect.KEYS, this.snapshotKeys);
  }

  @Override
  public ISeq values() {
    return accessSnapshot(Collect.VALS, this.snapshotVals);
  }

  @Override
  public IHashed assoc(final Object key, final Object val) {
    if (findKey(key) == null) {
      store(key, val);
      return this;
    }
    final ISeq entries = items();
    final IHashed newMap = new Map(key, val);
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
    if (e instanceof ISeq) {
      final ISeq entry = (ISeq) e;
      if (entry.size() == 2) {
        return assoc(entry.first(), entry.last());
      }
    }
    throw new IllegalArgumentException(str("expected |ISeq| == 2, got: %s", e));
  }
}