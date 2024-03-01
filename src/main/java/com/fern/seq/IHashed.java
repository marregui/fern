package com.fern.seq;

public interface IHashed extends ISeq {
  ISeq keys();
  ISeq values();
  Object get(Object key);
  boolean contains(Object key);
  IHashed assoc(Object key, Object val);
  IHashed assoc(Object key);
}