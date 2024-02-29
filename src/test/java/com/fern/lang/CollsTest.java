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

import org.junit.Test;
import com.fern.BaseTest;
import com.fern.seq.Colls;
import com.fern.seq.ISeq;
import static com.fern.lang.Fn.*;
import static com.fern.seq.Colls.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CollsTest extends BaseTest {
  static final ISeq makeSeq(int size) {
    return makeSeq(0, size);
  }
  
  static final ISeq makeSeq(int start, int end) {
    ISeq seq = newlist();
    for (int i=start; i< end; i++) {
      seq = cone(String.format("str_%d", i), seq);
    }
    return seq;
  }
  
  @Test
  public void testListAsFn() {
    final ISeq seq = newlist();
    assertNull(seq.invoke(null));
    expectFail(IllegalArgumentException.class, () -> { seq.invoke(); });
    expectFail(IllegalArgumentException.class, () -> { seq.invoke(2, 3); });
    final ISeq seq1 = newlist(2, null, "k");
    assertEquals(seq1.invoke(2), Integer.valueOf(0));
    assertEquals(seq1.invoke(null), Integer.valueOf(1));
    assertEquals(seq1.invoke("k"), Integer.valueOf(2));
    assertNull(seq.invoke(13));
  }
  
  @Test
  public void testSetAsFn() {
    final ISeq seq = newset();
    expectFail(IllegalArgumentException.class, () -> { seq.invoke(); });
    expectFail(IllegalArgumentException.class, () -> { seq.invoke(null); });
    expectFail(IllegalArgumentException.class, () -> { seq.invoke(2, 3); });
    final ISeq seq1 = newset(2, "k");
    assertEquals(seq1.invoke(2), 2);
    assertEquals(seq1.invoke("k"), "k");
    assertNull(seq.invoke(13));
  }
  
  @Test
  public void testMapAsFn() {
    final ISeq seq = newmap();
    expectFail(IllegalArgumentException.class, () -> { seq.invoke(); });
    expectFail(IllegalArgumentException.class, () -> { seq.invoke(null); });
    expectFail(IllegalArgumentException.class, () -> { seq.invoke(2, 3); });
    final ISeq seq1 = newmap(2, "k", "O", 1);
    assertEquals(seq1.invoke(2), "k");
    assertEquals(seq1.invoke("O"), 1);
    assertNull(seq.invoke(13));
  }
  
  @Test 
  public void testConcat() {
    assertEquals(concat(newlist(1,2,3,4), newlist(5,6,7,8), newlist(9), 10), newlist(1,2,3,4,5,6,7,8,9, 10));
  }
  
  @Test
  public void testNil(){
    assertEquals(Colls.nil(), newlist().rest());
    assertEquals(Colls.nil().hashCode(), newlist().rest().hashCode());
  }
  
  @Test
  public void testSome() {
    assertEquals(some(even, newlist(1, 1, 1, 1, 2)), Integer.valueOf(2));
    assertEquals(some(even, newlist(1, 1, 1, 1)), nil());
    assertEquals(some(even, newlist()), newlist());
    assertEquals(some(even, nil()), nil());
  }
  
  @Test
  public void testSomeXargs() {
    assertEquals(some(greater, newlist(1, 1, 1, 1, 2), 1), Integer.valueOf(2));
    assertEquals(some(greater, newlist(1, 1, 1, 1), 2), nil());
    assertEquals(some(greater, newlist(1, 1, 1, 1), 0), Integer.valueOf(1));
    assertEquals(some(greater, newlist(), 0), newlist());
    assertEquals(some(greater, nil()), nil());
  }
  
  @Test
  public void testRange() {
    assertEquals(range(101), range(0, 101, 1));
    assertEquals(newlist(), range(1, 1, 1));
    assertEquals(newlist(), range(1, 1, -1));
    assertEquals(newlist(), range(0));
    expectFail(IllegalArgumentException.class, () -> { range(12, 11, 1); });
    expectFail(IllegalArgumentException.class, () -> { range(11, 12, -1); });
  }
  
  @Test
  public void testTake() {
    assertEquals(take(3, makeSeq(3)), makeSeq(3));
    expectFail(IllegalArgumentException.class, () -> { take(-1, newlist()); });
    final ISeq seq = makeSeq(3);
    assertTrue(seq == take(1000, seq));
    assertEquals(take(2, nil()), nil());
    assertEquals(take(0, seq), newlist());
  }
  
  @Test
  public void testTakewhile() {
    assertEquals(takewhile(even, nil()), nil());
    assertEquals(takewhile(even, newlist()), newlist());
    assertEquals(takewhile(even, newlist(2, 2, 2, 2, 1, 0, 0, 0, 0)), newlist(2, 2, 2, 2));
    assertEquals(takewhile(even, newlist(1, 0, 0, 0, 0)), newlist());
  }
  
  @Test
  public void testTakewhileXargs() {
    assertEquals(takewhile(greater, nil(), 2), nil());
    assertEquals(takewhile(greater, newlist(), 2), newlist());
    assertEquals(takewhile(greater, newlist(2, 2, 2, 2, 1, 0, 0, 0, 0), 1), newlist(2, 2, 2, 2));
    assertEquals(takewhile(greater, newlist(1, 0, 0, 0, 0), -1), newlist(1, 0, 0, 0, 0));
    assertEquals(takewhile(greater, newlist(1, 0, 0, 0, 0), 1), newlist());
  }
  
  @Test
  public void testDrop() {
    assertEquals(drop(3, makeSeq(3)), newlist());
    expectFail(IllegalArgumentException.class, () -> { drop(-1, newlist()); });
    final ISeq seq = makeSeq(3);
    assertTrue(seq == drop(0, seq));
    assertEquals(drop(1000, seq), newlist());
    assertEquals(drop(1000, nil()), nil());
    assertEquals(drop(3, makeSeq(10)), makeSeq(3, 10));
  }
  
  @Test
  public void testDropwhile() {
    assertEquals(dropwhile(even, nil()), nil());
    assertEquals(dropwhile(even, newlist()), newlist());
    assertEquals(dropwhile(even, newlist(2, 2, 2, 2, 1, 0, 0, 0, 0)), newlist(1, 0, 0, 0, 0));
    assertEquals(dropwhile(even, newlist(1, 0, 0, 0, 0)), newlist(1, 0, 0, 0, 0));
    assertEquals(dropwhile(even, newlist(2, 2, 2, 2)), newlist());
  }
  
  @Test
  public void testDropwhileXargs() {
    assertEquals(dropwhile(greater, nil(), 2), nil());
    assertEquals(dropwhile(greater, newlist(), 2), newlist());
    assertEquals(dropwhile(greater, newlist(2, 2, 2, 2, 1, 0, 0, 0, 0), 1), newlist(1, 0, 0, 0, 0));
    assertEquals(dropwhile(greater, newlist(1, 0, 0, 0, 0), -1), newlist());
  }
}