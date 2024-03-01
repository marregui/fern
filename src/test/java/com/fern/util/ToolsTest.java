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
        assertTrue(noe(new Integer[]{}));
        assertTrue(noe(new int[]{}));
        assertTrue(noe(new String[]{}));
        assertFalse(noe(1));
        assertFalse(noe(" "));
        assertFalse(noe(new Integer[]{1}));
        assertFalse(noe(new int[]{1}));
    }

    @Test
    public void testSafeLen() {
        assertEquals(safeLen(null), -1);
        assertEquals(safeLen(1, 2, 3), 3);
        assertEquals(safeLen("Miguel", 3), 2);
    }
}