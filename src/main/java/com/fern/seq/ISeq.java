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