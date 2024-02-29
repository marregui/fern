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

import static com.fern.lang.Fn.*;
import static com.fern.seq.Colls.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import com.fern.BaseTest;
import com.fern.lang.FnBody;
import com.fern.lang.IFn;
import com.fern.seq.ISeq;

public class QuicksortTest extends BaseTest {
  
  final static IPredicate lessThanPivot = defpred(new FnBody<Boolean>(){
    @Override
    public Boolean fnBody() {
      return (int) arg(1) < (int) arg(2);
    }
  });
  
  final static IPredicate greaterEqualThanPivot = defpred(new FnBody<Boolean>(){
    @Override
    public Boolean fnBody() {
      return (int) arg(1) >= (int) arg(2);
    }
  });
  
  final static IFn<ISeq> qsort = defn(defargs(ISeq.class), new FnBody<ISeq>(){
    @Override
    public ISeq fnBody() {
      final ISeq seq = arg(1);
      if (isNil(seq) || seq.isEmpty()) {
        return seq;
      }
      final int pivot = (int) seq.first();
      final ISeq rest = seq.rest();
      final ISeq lesser = filter(lessThanPivot, rest, pivot);
      final ISeq greater = filter(greaterEqualThanPivot, rest,  pivot);
      return concat(qsort.invoke(lesser), pivot, qsort.invoke(greater));
    }
  });
  
  public static int [] quicksort(final int [] array){
    quicksort(0, array.length - 1, array);
    return array;
  }
  
  public static void quicksort(final int low, final int high, final int [] array){
    if (array == null || array.length == 0 || high <= low) {
      return;
    }
    final int pivot = array[low + (high - low) / 2];
    int left = low;
    int right = high;
    while (left < right){
      while (array[left] < pivot) {
        left++;
      }
      while (array[right] > pivot) {
        right--;
      }
      if (left <= right){
        final int t = array[left];
        array[left] = array[right];
        array[right] = t;
        left++;
        right--;
      }
    }
    if (left < high) {
      quicksort(left, high, array);
    }
    if (right > low) {
      quicksort(low, right, array);
    }
  }
  
  static ISeq asList(final int [] array) {
    ISeq list = newlist();
    for (int e: array) {
      list = list.cone(e);
    }
    return list;
  }
  
  @Test
  public void testQuicksort() {
    assertEquals(qsort.invoke(newlist(3, 1, 6, 1, 0, 9, 2)), newlist(0, 1, 1, 2, 3, 6, 9));
    assertTrue(Arrays.equals(quicksort(new int []{3, 1, 6, 1, 0, 9, 2}), new int[]{0, 1, 1, 2, 3, 6, 9}));
    final int arraySize = 10000;
    final int [] array = genarray(arraySize, 0, arraySize);
    ISeq seq = asList(array.clone());
    assertEquals(asList(quicksort(array)), qsort.invoke(seq));
  }
  
  @Test
  public void testPerformanceBaseline() {
    final int sampleSize = 300;
    final int arraySize = 2000;
    final Avg classicImpl = new Avg();
    for (int i=0; i < sampleSize; i++) {
      int [] array = genarray(arraySize, 0, arraySize);
      classicImpl.addPoint(timed(() -> quicksort(array)));
    }
    final Avg fernImpl = new Avg();
    for (int i=0; i < sampleSize; i++) {
      ISeq seq = asList(genarray(arraySize, 0, arraySize));
      fernImpl.addPoint(timed(() -> qsort.invoke(seq)));
    }
    final double classic = classicImpl.getAvg();
    final double fern = fernImpl.getAvg();
    final double factor = fern / classic;
    System.out.printf("Sample |%d|, array |%d| classic -> %.3f, fern -> %.3f, factor -> %.3f\n", 
        sampleSize, arraySize, classic, fern, factor);
  }
  
  public static int [] genarray(final int size, final int min, final int max){
    final int [] array = new int[size];
    final Random rand = new Random();
    final int upper = max - min + 1;
    for (int i=0; i < size; i++) {
      array[i] = min + rand.nextInt(upper);
    }
    return array;
  }
}