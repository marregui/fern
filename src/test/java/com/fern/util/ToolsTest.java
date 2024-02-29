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

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static com.fern.util.Tools.noe;
import static com.fern.util.Tools.safeLen;
import static org.junit.Assert.assertEquals;

public class ToolsTest {
  @Test
  public void testNoe() {
    assertTrue(noe(null));
    assertTrue(noe(""));
    assertTrue(noe(new Integer[] {}));
    assertTrue(noe(new int[] {}));
    assertTrue(noe(new String[] {}));
    assertFalse(noe(1));
    assertFalse(noe(" "));
    assertFalse(noe(new Integer[] { 1 }));
    assertFalse(noe(new int[] { 1 }));
  }
  
  @Test
  public void testSafeLen() {
    assertEquals(safeLen(null), -1);
    assertEquals(safeLen(1, 2, 3), 3);
    assertEquals(safeLen("Miguel", 3), 2);
  }
}