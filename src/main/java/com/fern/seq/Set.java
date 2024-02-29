/**
 * Copyright (c) Miguel Arregui. All rights reserved.
 * 
 * The use and distribution terms for this software are covered by the
 * 
 * Apache License 2.0
 * (https://opensource.org/licenses/Apache-2.0)
 * 
 * available in the LICENSE file at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound
 * by the terms of this license. You must not remove this notice, or
 * any other, from this software.
 **/
package com.fern.seq;

import java.util.Iterator;

public class Set extends ABaseHashed {
  
  public static IHashed neu(final Object... entries) {
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
      return this.key;
    }
    
    @Override
    public Object last() {
      return this.key;
    }
    
    @Override
    public String toString() {
      return String.format("[%s]", this.key);
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
      if (null == o || false == (o instanceof ISeq)) {
        return false;
      }
      final ISeq that = (ISeq) o;
      return (this.key == that.first() || this.key != null && this.key.equals(that.first()));
    }
    
    @Override
    public int hashCode() {
      return null != this.key? this.key.hashCode() : 0;
    }
    
    @Override
      public Iterator<Object> iterator() {
        return new Iterator<Object>() {
          private int idx = 0;

          @Override
          public final boolean hasNext() {
            return this.idx < 1;
          }

          @Override
          public Object next() {
            if (hasNext()) {
              this.idx++;
              return key;
            }
            throw new IndexOutOfBoundsException();
          }
        };
      }
  }
  
  private Set(final Object... entries) {
    for (int i = 0; i < entries.length; i++) {
      store(entries[i], null);
    }
  }

  @Override
  void storeInBucket(final IHashedEntry[] bucket, final Object key, final Object val) {
    for (int i = 0; i < bucket.length; i++) {
      if (bucket[i] == null) {
        bucket[i] = new Entry(key);
        this.size.incrementAndGet();
        this.snapshotEntries.set(null);
        this.quickToArray.set(null);
        break;
      }
      else if (bucket[i].first().equals(key)) {
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
  public Object [] toArray() {
    Object [] array = this.quickToArray.get();
    if (array == null) {
      synchronized (this.quickToArray) {
        array = this.quickToArray.get();
        if (array == null) {
          final ISeq items = items();
          array = new Object[items.size()];
          for (int i=0; i < array.length; i++) {
            array[i] = ((ISeq) items.nth(i)).first();
          }
          this.quickToArray.set(array);
        }
      }
    }
    return this.quickToArray.get();
  }

  @Override
  public IHashed assoc(final Object entry) {
    if (findKey(entry) == null) {
      store(entry, null);
      return this;
    }
    final ISeq entries = items();
    final IHashed newSet = new Set(entry);
    for (int i = 0; i < entries.size(); i++) {
      final ISeq nthEntry = (ISeq) entries.nth(i);
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