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

import java.util.Comparator;
import java.util.Iterator;

class ABaseSeq implements ISeq {
  static final ISeq NIL = new ABaseSeq(){ /* defaults */ };
  static final String TO_STR_SEP = ", ";
  static final Comparator<Object> DEFAULT_COMPARATOR = (o1, o2) -> {
	  if (o1 == null && o2 == null) { return 0; }
	  if (o1 == null) { return -1; }
	  if (o2 == null) { return 1; }
	  return o1.toString().compareTo(o2.toString());
  };
	  
  ABaseSeq() {
    /* defaults */
  }
  
  @Override
  public String toString() {
    return "NIL";
  }
  
  @Override
  public int hashCode() {
    return 0;
  }
  
  @Override
  public boolean equals(Object o) {
    return o == NIL;
  }
  
  @Override
  public Object invoke(Object... args){
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Object first() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object last() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object nth(int n) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ISeq rest() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public ISeq items() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ISeq cons(Object e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ISeq cone(Object e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ISeq sorted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ISeq sorted(Comparator<Object> comparator) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Iterator<Object> iterator() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Object [] toArray() {
    throw new UnsupportedOperationException();
  }
}