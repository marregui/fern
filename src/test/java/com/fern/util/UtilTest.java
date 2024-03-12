package com.fern.util;

import org.junit.Test;

import static com.fern.util.Util.*;
import static org.junit.Assert.*;

public class UtilTest {
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

    @Test
    public void testStr() {
        assertNull(str(null));
        assertEquals("ramon", str("ramon", 2, 3));
        assertEquals("ramon\n", str("ramon\n", 2, 3));
        assertEquals("ramon\n", str("ramon%n", 2, 3));
        assertEquals("ramon\n", str("ramon%n", 2, 3));
        assertEquals("ramon%", str("ramon%%", 2, 3));
        assertEquals("ram%%%%on%", str("ram%%%%%%%%on%%", 2, 3));
        assertEquals("ram%%2%%on%", str("ram%%%%%d%%%%on%%", 2, 3));
    }
}