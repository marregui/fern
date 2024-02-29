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

public interface IHashed extends ISeq {
  ISeq keys();
  ISeq values();
  Object get(Object key);
  boolean contains(Object key);
  IHashed assoc(Object key, Object val);
  IHashed assoc(Object key);
}