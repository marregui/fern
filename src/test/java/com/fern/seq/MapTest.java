package com.fern.seq;

import org.junit.Test;
import com.fern.BaseTest;
import com.fern.seq.Colls;
import com.fern.seq.IHashed;
import com.fern.seq.ISeq;
import com.fern.seq.List;
import com.fern.seq.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class MapTest extends BaseTest {
    @Test
    public void testFailedConstructor() {
        expectFail(NullPointerException.class, () -> {
            Map.neu((Object[]) null);
        });
        expectFail(IllegalArgumentException.class, () -> {
            Map.neu(1, 2, 3);
        });
        expectFail(UnsupportedOperationException.class, () -> {
            Map.neu(1, 2).assoc(3);
        });
    }

    @Test
    public void testConstructor() {
        final IHashed m = Map.neu(
                ":name", "miguel",
                ":title", "mr",
                ":age", 214,
                ":extra", null,
                "", "");

        assertTrue(m.size() == 5);
        assertEquals(m.keys().sorted(), List.neu("", ":age", ":extra", ":name", ":title"));
        assertEquals(m.values().sorted(), List.neu(null, "", 214, "miguel", "mr"));
        assertTrue(m.contains(":name"));
        assertTrue(m.contains(":title"));
        assertTrue(m.contains(":age"));
        assertTrue(m.contains(":extra"));
        assertTrue(m.contains(""));
        assertFalse(m.contains("not_contained"));
        assertEquals(m.get(":name"), "miguel");
        assertEquals(m.get(":title"), "mr");
        assertEquals(m.get(":age"), 214);
        assertNull(m.get(":extra"));
        assertEquals(m.get(""), "");
        final ISeq v = List.neu(
                List.neu("", ""),
                List.neu(":age", 214),
                List.neu(":extra", null),
                List.neu(":name", "miguel"),
                List.neu(":title", "mr"));

        System.out.printf("m: %s\n", m.items().sorted());
        System.out.printf("v: %s\n", v);
        assertEquals(m.items().sorted(), v);
        assertEquals(v, m.items().sorted());
    }

    @Test
    public void testEmptyConstructor() {
        assertTrue(Map.neu().size() == 0);
        assertNull(Map.neu().first());
        expectFail(UnsupportedOperationException.class, () -> Map.neu().rest().first());
        assertEquals(Map.neu().rest(), Colls.nil());
        assertTrue(Map.neu().rest() == Colls.nil());
    }

    @Test
    public void testRepeatedEntryConstructor() {
        final IHashed m = Map.neu(":name", "miguel", ":title", "mr", ":name", "");
        assertTrue(m.size() == 2);
        assertEquals(m.get(":name"), "");
    }

    @Test
    public void testAssoc() {
        final IHashed m = Map.neu();
        for (String k : new String[]{
                "alpha", "beta", "gamma", "epsilon", "zeta", "eta", "theta"
        }) {
            final IHashed nm = m.assoc(k, k);
            assertTrue(nm == m);
            assertEquals(nm, m);
            assertEquals(m, nm);
        }
        final IHashed nm = m.assoc("alpha", "alpha");
        assertFalse(nm == m);
        assertEquals(nm, m);
        assertEquals(m, nm);
    }

    @Test
    public void testNth() {
        final IHashed m = Map.neu(1, "one", 2, "two");
        assertEquals(m.first(), List.neu(1, "one"));
        assertEquals(m.last(), List.neu(2, "two"));
        assertEquals(m.nth(0), m.first());
        assertEquals(m.nth(m.size() - 1), m.last());
    }

    @Test
    public void testToArray() {
        IHashed m = Map.neu(1, "uno", 2, "dos", 3, "tres", 4, "cuatro");
        final Object[] array1 = m.toArray();
        m.cons(List.neu(5, "cinco"));
        m.cone(List.neu(6, "seis"));
        final Object[] array2 = m.toArray();
        assertEquals(m, Map.neu(array2));
        m.cons(List.neu(7, "siete"));
        m.cone(List.neu(8, "ocho"));
        final Object[] array3 = m.toArray();
        assertEquals(m, Map.neu(array3));
        assertFalse(Arrays.equals(array1, array2));
        assertFalse(Arrays.equals(array1, array3));
        assertFalse(Arrays.equals(array2, array3));
        assertEquals(Map.neu(1, "uno", 2, "dos", 3, "tres", 4, "cuatro"), Map.neu(array1));
    }
}