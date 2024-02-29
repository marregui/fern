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
package com.fern.util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Produces unique ids
 */
public final class UniqueName implements Iterable<String>, Iterator<String> {
  private static final String DEFAULT_NAME_SPACE = "dns_";
  private static final int DEFAULT_NUM_DIGITS = 5;
  private static final int GROW_BY_DIGITS = 2;
  private static final byte FIRST_ALPHA = (byte) 'a';
  private static final byte LAST_ALPHA = (byte) 'z';

  // singleton pattern
  private static final class SingletonInstantiator {
    static final UniqueName UNIQUE_INSTANCE = new UniqueName();
  }

  public static final UniqueName get() {
    return SingletonInstantiator.UNIQUE_INSTANCE;
  }

  private final String nameSpace;
  private byte[] offsets;
  private int numDigits;
  private final StringBuilder sb;

  private UniqueName() {
    this.nameSpace = DEFAULT_NAME_SPACE;
    this.numDigits = DEFAULT_NUM_DIGITS;
    this.offsets = new byte[this.numDigits];
    Arrays.fill(this.offsets, FIRST_ALPHA);
    this.sb = new StringBuilder(this.numDigits * 2);
    this.sb.append(DEFAULT_NAME_SPACE);
  }

  @Override
  public final synchronized String toString() {
    this.sb.setLength(this.nameSpace.length());
    for (int i = 0; i < this.numDigits; i++) {
      this.sb.append((char) this.offsets[i]);
    }
    return this.sb.toString();
  }

  @Override
  public Iterator<String> iterator() {
    return this;
  }

  @Override
  public synchronized boolean hasNext() {
    return this.offsets[this.numDigits - 1] <= LAST_ALPHA;
  }

  @Override
  public synchronized String next() {
    if (hasNext()) {
      final String name = toString();
      moveToNext(0);
      return name;
    }
    throw new IndexOutOfBoundsException();
  }

  private void moveToNext(int idx) {
    if (idx < this.numDigits) { // stop recursion otherwise
      this.offsets[idx]++;
      if (this.offsets[idx] > LAST_ALPHA) { // check overflow and perform carryover
        if (idx < this.numDigits - 1) {
          // carry over is done if we have enough digits
          this.offsets[idx] = FIRST_ALPHA;
          moveToNext(idx + 1);
        }
        else {
          // otherwise grow and reset
          final int newNumDigits = this.numDigits + GROW_BY_DIGITS;
          final byte[] newOffsets = new byte[newNumDigits];
          System.arraycopy(this.offsets, 0, newOffsets, 0, this.numDigits);
          Arrays.fill(newOffsets, FIRST_ALPHA);
          this.offsets = newOffsets;
          this.numDigits = newNumDigits;
        }
      }
    }
  }
}