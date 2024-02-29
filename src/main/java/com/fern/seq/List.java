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

import static com.fern.util.Tools.safeLen;
import static com.fern.util.Tools.str;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class List extends ABaseSeq {
	
  private static final Object AVAILABLE_SLOT = new Object() {
	  @Override
	  public String toString() {
		  return "AVAILABLE_SLOT";
	  }
  };
  private static final int RESIZE_EXTRA_SLOTS = 15;
  
  public static List neu(final Object... elements) {
    if (elements == null) {
      throw new NullPointerException();
    }
    return new List(elements);
  }

  private final int start;
  private final int end;
  private final int size;
  private final Object[] elements;
  private final int hashCode;
  private final AtomicReference<Object[]> quickToArray;
  private final AtomicReference<String> quickStr; // acts as cons lock
  private final AtomicReference<ISeq> quickRest;  // acts as cone lock

  public List(final Object... elements) {
    this(0, safeLen(elements), elements);
  }

  public List(final int start, final int end, final Object... elements) {
    if (elements == null) {
      throw new NullPointerException();
    }
    if (start < 0 || end < start || end > elements.length) {
      throw new IllegalArgumentException(str(
    		  "bad range [%d, %d], elements.length is %d", 
    		  start, end, elements.length));
    }
    this.hashCode = hashCode(start, end, elements);
    this.size = end - start;
    this.start = start;
    this.end = end;
    this.elements = elements;
    this.quickStr = new AtomicReference<>();
    this.quickRest = new AtomicReference<>();
    this.quickToArray = new AtomicReference<>();
  }

  private static int hashCode(final int start, final int end, final Object[] array) {
    int result = 11;
    for (int i = start; i < end; i++) {
      final Object el = array[i];
      result = 31 * result + (el == null ? 0 : el.hashCode());
    }
    return result;
  }
  
  /**
   * Takes zero or one parameter.
   * <p>
   * No parameters: if the list contains null it will return its position, otherwise null.
   * <br/>
   * One parameter: will search for the parameter returning its position, otherwise null.
   * @param args,  
   * @return null or the position of the first arg in the list
   */
  @Override
  public Object invoke(Object... args){
    if (args != null && (args.length == 0 || args.length > 1)) {
      throw new IllegalArgumentException("only one arg is allowed, to return its position if found, or null");
    }
    final Object target = (args != null)? args[0] : null; 
    for (int i=this.start; i < this.end; i++) {
      final Object o = this.elements[i];
      if ((o == null && target == null) || (o != null && target != null && o.equals(target))) {
        return i;
      }
    }
    return null;
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || false == (o instanceof ISeq)) {
      return false;
    }
    final ISeq that = (ISeq) o;
    if (this.size != that.size()) {
      return false;
    }
    final Iterator<Object> thatIterator = that.iterator();
    for (int i = this.start; i < this.end && thatIterator.hasNext(); i++) {
      final Object e1 = this.elements[i];
      final Object e2 = thatIterator.next();
      if (false == (e1 == null ? e2 == null : e1.equals(e2))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    String str = this.quickStr.get();
    if (str == null) {
      synchronized (this.quickStr) {
        str = this.quickStr.get();
        if (str == null) {
          final StringBuilder sb = new StringBuilder("[");
          for (int i = this.start; i < this.end; i++) {
            sb.append(this.elements[i]).append(TO_STR_SEP);
          }
          if (this.size > 0) {
            sb.setLength(sb.length() - TO_STR_SEP.length());
          }
          sb.append("]");
          this.quickStr.set(sb.toString());
        }
      }
    }
    return this.quickStr.get();
  }

  @Override
  public Object first() {
    return this.size == 0 ? null : this.elements[this.start];
  }

  @Override
  public Object last() {
    return this.size == 0 ? null : this.elements[this.end - 1];
  }

  @Override
  public Object nth(final int n) {
    final int offset = this.start + n;
    if (offset >= this.start && offset < this.end) {
      return this.elements[offset];
    }
    throw new IndexOutOfBoundsException();
  }

  @Override
  public int size() {
    return this.size;
  }
  
  @Override
  public boolean isEmpty() {
    return this.size == 0;
  }

  @Override
  public ISeq rest() {
    ISeq rest = this.quickRest.get();
    if (rest == null) {
      synchronized (this.quickRest) {
        rest = this.quickRest.get();
        if (rest == null) {
          switch (this.size) {
            case 0:
            case 1:
              this.quickRest.set(NIL);
              break;
            default:
              this.quickRest.set(new List(this.start + 1, this.end, this.elements));
          }
        }
      }
    }
    return this.quickRest.get();
  }

  @Override
  public ISeq cons(final Object e) {
    boolean filledAvailableSlot = false;
    synchronized (this.quickStr) {
      if (this.start > 0 && this.elements[this.start - 1] == AVAILABLE_SLOT) {
        this.elements[this.start - 1] = e;
        filledAvailableSlot = true;
      }
    }
    ISeq array = null;
    if (filledAvailableSlot) {
      array = new List(this.start - 1, this.end, this.elements);
    }
    else {
      final Object[] els = new Object[RESIZE_EXTRA_SLOTS + this.size];
      final int headOfNewList = RESIZE_EXTRA_SLOTS;
      Arrays.fill(els, 0, headOfNewList, AVAILABLE_SLOT);
      els[headOfNewList - 1] = e;
      System.arraycopy(this.elements, this.start, els, headOfNewList, this.size);
      array = new List(headOfNewList - 1, els.length, els);
    }
    return array;
  }

  @Override
  public ISeq cone(final Object e) {
    boolean filledAvailableSlot = false;
    synchronized (this.quickRest) {
      if (this.end < this.elements.length && this.elements[this.end] == AVAILABLE_SLOT) {
        this.elements[this.end] = e;
        filledAvailableSlot = true;
      }
    }
    ISeq array = null;
    if (filledAvailableSlot) {
      array = new List(this.start, this.end + 1, this.elements);
    }
    else {
      final Object[] els = new Object[this.size + RESIZE_EXTRA_SLOTS];
      System.arraycopy(this.elements, this.start, els, 0, this.size);
      els[this.end] = e;
      Arrays.fill(els, this.end + 1, this.end + RESIZE_EXTRA_SLOTS, AVAILABLE_SLOT);
      array = new List(this.start, this.end + 1, els);
    }
    return array;
  }
  
  @Override
  public Object [] toArray() {
    Object[] array = this.quickToArray.get();
    if (array == null) {
      synchronized (this.quickToArray) {
        array = this.quickToArray.get();
        if (array == null) {
          array = new Object[this.size];
          System.arraycopy(this.elements, this.start, array, 0, this.size);
          this.quickToArray.set(array);
        }
      }
    }
    return this.quickToArray.get();
  }

  @Override
  public Iterator<Object> iterator() {
    return new Iterator<Object>() {
      private int idx = List.this.start;

      @Override
      public final boolean hasNext() {
        return this.idx < List.this.end;
      }

      @Override
      public Object next() {
        if (hasNext()) {
          return List.this.elements[this.idx++];
        }
        throw new IndexOutOfBoundsException();
      }
    };
  }

  @Override
  public ISeq sorted() {
    return sorted(DEFAULT_COMPARATOR);
  }

  @Override
  public ISeq sorted(Comparator<Object> comparator) {
    final Object[] els = new Object[this.size];
    System.arraycopy(this.elements, this.start, els, 0, this.size);
    Arrays.sort(els, comparator);
    return new List(els);
  }
}