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
package com.fern.lang;

class Stack {
  private static final int INIT_SIZE = 20;
  private static final int GROWTH_FACTOR = 2;
  
  private Object[][] stack;
  private int offset;
  
  Stack() {
    this.stack = new Object[INIT_SIZE][];
    this.offset = 0;
  }
  
  synchronized int size() {
    return this.stack.length;
  }
  
  synchronized int offset() {
    return this.offset;
  }
  
  void push(final Object [] e) {
    if (e == null) {
      throw new IllegalArgumentException("cannot push null");
    }
    synchronized(this) {
      if (this.offset == this.stack.length) {
        final Object [] newStack[] = new Object[this.stack.length * GROWTH_FACTOR][];
        System.arraycopy(this.stack, 0, newStack, 0, this.stack.length);
        this.stack = newStack;
      }
      this.stack[this.offset++] = e;
    }
  }
  
  Object [] replaceTop(final Object [] e) {
    if (e == null) {
      throw new IllegalArgumentException("cannot replace with null");
    }
    Object [] prev = null;
    synchronized(this) {
      if (this.offset > 0) {
        prev = this.stack[this.offset - 1]; 
        this.stack[this.offset - 1] = e;
      }
    }
    return prev;
  }
  
  Object [] peek() {
    Object [] e = null;
    synchronized(this) {
      if (this.offset > 0) {
        e = this.stack[this.offset - 1];
      }
    }
    return e;
  }

  Object [] pop() {
    Object [] e = null;
    synchronized(this) {
      if (this.offset > 0) {
        e = this.stack[this.offset - 1];
        this.stack[this.offset - 1] = null;
        this.offset --;
      }
    }
    return e;
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("[<");
    synchronized (this) {
      sb.append(this.offset).append(">|");
      for (int i=0; i < this.offset; i++) {
        final Object [] e = this.stack[i];
        if (e != null) {
          for (int j=0; j < e.length; j++) {
            sb.append(e[j]).append(",");
          }
          if (e.length > 0) {
            sb.setLength(sb.length() - 1);
          }
        }
        else {
          sb.append("null");
        }
        sb.append("|");
      }
      if (this.offset > 0) {
        sb.setLength(sb.length() - 1);
      }
    }
    sb.append("]");
    return sb.toString();
  }
}