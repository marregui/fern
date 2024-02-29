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
import com.fern.lang.Invocable;

public interface ISeq extends Iterable<Object>, Invocable<Object> {
	
  int size();
  boolean isEmpty();
  
  Object first();
  Object last();
  ISeq rest();
  Object nth(int n);
  ISeq items();
  
  ISeq cons(Object e);
  ISeq cone(Object e);
  
  ISeq sorted();
  ISeq sorted(Comparator<Object> comparator);
  
  Object [] toArray();
}