package com.fern.seq;

import org.junit.Test;
import com.fern.BaseTest;
import com.fern.seq.Colls;
import com.fern.seq.IHashed;
import com.fern.seq.ISeq;
import com.fern.seq.List;
import com.fern.seq.Map;
import com.fern.seq.Set;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class SetTest extends BaseTest {
    @Test
    public void testFailedConstructor() {
        expectFail(NullPointerException.class, () -> {
            Set.neu((Object[]) null);
        });
        expectFail(NullPointerException.class, () -> {
            Set.neu(1, 2, null);
        });
        expectFail(UnsupportedOperationException.class, () -> {
            Set.neu(1, 2).assoc(3, 4);
        });
    }

    @Test
    public void testEmptyConstructor() {
        assertTrue(Set.neu().size() == 0);
        assertNull(Set.neu().first());
        assertEquals(Set.neu().rest(), Colls.nil());
        assertTrue(Set.neu().rest() == Colls.nil());
    }

    @Test
    public void testConstructor() {
        final IHashed m = Set.neu(":name", ":title", ":age", ":extra", "");
        assertTrue(m.size() == 5);
        assertEquals(m.items().sorted(), List.neu(
                List.neu(":age"),
                List.neu(":extra"),
                List.neu(":name"),
                List.neu(":title"),
                List.neu("")));

        assertTrue(m.contains(":name"));
        assertTrue(m.contains(":title"));
        assertTrue(m.contains(":age"));
        assertTrue(m.contains(":extra"));
        assertTrue(m.contains(""));
        assertFalse(m.contains("not_contained"));
        assertEquals(m.get(":name"), ":name");
        assertEquals(m.get(":title"), ":title");
        assertEquals(m.get(":age"), ":age");
        assertEquals(m.get(":extra"), ":extra");
        assertEquals(m.get(""), "");
    }

    @Test
    public void testRepeatedEntryConstructor() {
        final IHashed m = Set.neu(":name", ":title", ":name", "");
        assertTrue(m.size() == 3);
    }

    @Test
    public void testAssoc() {
        final IHashed m = Set.neu();
        for (String k : new String[]{"alpha", "beta", "gamma", "epsilon", "zeta", "eta", "theta"}) {
            final IHashed nm = m.assoc(k);
            assertTrue(nm == m);
            assertEquals(nm, m);
            assertEquals(m, nm);
        }
        final IHashed nm = m.assoc("alpha");
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
        ISeq s = Set.neu("gamma", "beta", "theta", "alpha");
        final Object[] array1 = s.toArray();
        s.cons("2");
        s.cone("4");
        final Object[] array2 = s.toArray();
        assertEquals(s, Set.neu(array2));
        s.cons("1");
        s.cone("5");
        final Object[] array3 = s.toArray();
        assertEquals(s, Set.neu(array3));
        assertFalse(Arrays.equals(array1, array2));
        assertFalse(Arrays.equals(array1, array3));
        assertFalse(Arrays.equals(array2, array3));
        assertEquals(Set.neu("gamma", "beta", "theta", "alpha"), Set.neu(array1));
    }

    @Test
    public void testSorted() {
        final ISeq s = Set.neu("gamma", "beta", "theta", "alpha");
        assertEquals(s, Set.neu("theta", "alpha", "beta", "gamma", "alpha"));
        assertEquals(s.sorted(), List.neu(List.neu("alpha"), List.neu("beta"), List.neu("gamma"), List.neu("theta")));
        ISeq l = List.neu();
        for (String e : new String[]{"theta", "gamma", "beta", "alpha"}) {
            l = l.cons(List.neu(e));
        }
        assertEquals(s.sorted(), l);
    }
}