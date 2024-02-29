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

public class Colls {
  // ===============================
  // =    N I L    /    T E S T    =
  // ===============================
  public static final boolean isNil(final ISeq seq) {
    return seq == ABaseSeq.NIL;
  }
  
  public static final ISeq nil() {
    return ABaseSeq.NIL;
  }
  
  // ====================================
  // =    S E Q    F A C T O R I E S    =
  // ====================================
  public static final ISeq newlist(final Object... elements) {
    return List.neu(elements);
  }
  
  public static final IHashed newset(final Object... entries) {
    return Set.neu(entries);
  }
  
  public static final IHashed newmap(final Object... keyValPairs) {
    return Map.neu(keyValPairs);
  }
  
  // ===============================================
  // =    S E Q    A C C E S S    M E T H O D S    =
  // ===============================================
  public static final int size(final ISeq seq) {
    return seq.size();
  }
  
  public static final boolean isEmpty(final ISeq seq) {
    return seq.isEmpty();  
  }
  
  public static final Object first(final ISeq seq) {
    return seq.first();
  }
  
  public static final Object last(final ISeq seq) {
    return seq.last();
  }
  
  public static final ISeq rest(final ISeq seq) {
    return seq.rest();  
  }
  
  public static final Object nth(final int n, final ISeq seq) {
    return seq.nth(n);
  }
  
  public static final ISeq cons(final Object e, final ISeq seq) {
    return seq.cons(e);
  }
  
  public static final ISeq cone(final Object e, final ISeq seq) {
    return seq.cone(e);
  }
  
  public static final ISeq sorted(final ISeq seq) {
    return seq.sorted();
  }
  
  public static final ISeq sorted(final ISeq seq, final Comparator<Object> comparator) {
    return seq.sorted(comparator);
  }
  
 // =====================================================
 // =    H A S H E D    A C C E S S    M E T H O D S    =
 // =====================================================
  public static final ISeq keys(final IHashed hash) {
    return hash.keys();
  }
  
  public static final ISeq values(final IHashed hash) {
    return hash.values();
  }
  
  public static final Object get(final IHashed hash, final Object key) {
    return hash.get(key);
  }
  
  public static final boolean contains(final IHashed hash, final Object key) {
    return hash.contains(key);
  }
  
  public static final IHashed assoc(final IHashed hash, final Object key, Object val) {
    return hash.assoc(key, val);
  }
  
  public static final IHashed assoc(final IHashed hash, final Object key) {
    return hash.assoc(key);
  }
}