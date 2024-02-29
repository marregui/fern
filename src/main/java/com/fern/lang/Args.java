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

import static com.fern.util.Tools.noe;
import java.util.Arrays;

final class Args {
  private final Class<?>[] defs;
  private final boolean lastArgIsVararg;

  Args(final boolean lastArgIsVararg, final Class<?>... defs) {
    for (int i=0; i < defs.length; i++) {
      if (noe(defs[i])) {
        throw new NullPointerException("nulls not allowed");
      }
    }
    this.defs = defs;
    this.lastArgIsVararg = lastArgIsVararg;
  }
  
  final String moniker() {
    String argsDesc = "";
    if (this.defs.length > 0) {
      final StringBuilder sb = new StringBuilder();
      for (int i=0; i < this.defs.length; i++) {
        final Class<?> clazz = this.defs[i];
        sb.append(clazz.getSimpleName()).append(", ");
      }
      sb.setLength(sb.length() - 2);
      if (this.lastArgIsVararg) {
        sb.append("*");
      }
      argsDesc = sb.toString();
    }
    return argsDesc;
  }
  
  final boolean isLastArgVararg() {
    return this.lastArgIsVararg;
  }
  
  final int size() {
    return this.defs.length;
  }
  
  final Class<?> get(final int i) {
    if (i >= 0 && i < this.defs.length) {
      return this.defs[i];
    }
    throw new IndexOutOfBoundsException();
  }
  
  @Override
  public final int hashCode() {
    return this.defs.hashCode() + (this.lastArgIsVararg? 1 : 0);
  }
  
  @Override
  public final boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || false == o instanceof Args) {
      return false;
    }
    final Args that = (Args) o;
    return this.lastArgIsVararg == that.lastArgIsVararg && Arrays.equals(this.defs, that.defs);
  }
  
  final Args from(final int idx) {
    if (idx >= 0 && (this.lastArgIsVararg || idx <= size())) {
      if (this.lastArgIsVararg && idx >= size()) {
        return new Args(this.lastArgIsVararg, this.defs[this.defs.length - 1]);
      } 
      final Class<?> [] array = new Class<?>[this.defs.length - idx];
      System.arraycopy(this.defs, idx, array, 0, array.length);
      return new Args(this.lastArgIsVararg, array);
    }
    throw new IndexOutOfBoundsException();
  }
  
  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    for (int i=0; i < this.defs.length; i++) {
      sb.append("$").append(i+1).append(" ").append(this.defs[i]).append(", ");
    }
    if (this.defs.length > 0) {
      sb.setLength(sb.length() - 2);
      if (this.lastArgIsVararg) {
        sb.append("*");
      }
    }
    return sb.toString();
  }
}