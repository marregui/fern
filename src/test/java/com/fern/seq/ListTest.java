package com.fern.seq;

import org.junit.Test;
import com.fern.BaseTest;
import com.fern.seq.Colls;
import com.fern.seq.ISeq;
import com.fern.seq.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class ListTest extends BaseTest {
    @Test
    public void testFailedConstructor() {
        expectFail(NullPointerException.class, () -> List.neu((Object[]) null));
    }

    @Test
    public void testConstructor() {
        // Empty vector
        assertTrue(List.neu().size() == 0);
        assertNull(List.neu().first());
        assertEquals(List.neu().rest(), Colls.nil());

        // first/last/rest
        final ISeq s1 = List.neu(1, "e2");
        assertEquals(s1.size(), 2);
        assertEquals(s1.first(), 1);
        assertEquals(s1.last(), "e2");
        assertEquals(List.neu(s1.first()), List.neu(1));
        assertEquals(List.neu(s1.last()), List.neu("e2"));
        assertEquals(s1.rest().first(), s1.last());
        assertEquals(s1.rest(), List.neu("e2"));
        assertEquals(s1.rest().rest(), Colls.nil());
        expectFail(UnsupportedOperationException.class, () -> s1.rest().rest().first());
        expectFail(UnsupportedOperationException.class, () -> s1.rest().rest().last());

        // equality
        ISeq ss = List.neu("e1");
        assertEquals(ss.first(), ss.last());
        assertTrue(ss.first() == ss.last());
        assertNull(List.neu().first());
        assertNull(List.neu().last());

        // cone
        ss = List.neu();
        for (int i = 0; i < 5; i++) {
            ss = ss.cone(i);
        }
        assertEquals(ss, List.neu(0, 1, 2, 3, 4));

        // cons
        ss = List.neu();
        for (int i = 0; i < 5; i++) {
            ss = ss.cons(i);
        }
        assertEquals(ss, List.neu(4, 3, 2, 1, 0));

        // nth
        ss = List.neu(1, 2, 3, 4, 5);
        for (int i = 1; i <= 5; i++) {
            assertEquals(ss.nth(i - 1), i);
        }
        ss = ss.rest().rest().rest();
        assertEquals(ss.first(), ss.nth(0));
        assertEquals(ss.last(), ss.nth(1));

        // iterator
        int i = 1;
        for (Object e : List.neu(1, 2, 3, 4, 5)) {
            assertEquals(e, i++);
        }

        // equals
        assertEquals(List.neu(1, 2, 3, 4, 5), List.neu(1, 2, 3, 4, 5));
        ss = List.neu(1, 2, 3, 4, 5);
        assertEquals(List.neu(4, 5), ss.rest().rest().rest());

        // sorted
        ss = List.neu(7, 1, 4, 3, 6, 8, 1);
        assertEquals(List.neu(1, 1, 3, 4, 6, 7, 8), ss.sorted());
    }

    @Test
    public void testToArray() {
        ISeq a1 = List.neu("1", "1", "1", "1", "1");
        final Object[] array = a1.toArray();
        a1.cons("2");
        a1.cone("4");
        assertEquals(a1, List.neu(array));
    }

    @Test
    public void testCons() {
        ISeq a1 = List.neu("1", "1", "1", "1", "1");
        for (int i = 0; i < 5; i++) {
            a1 = a1.cons("2");
        }
        ISeq a2 = List.neu("2", "2", "2", "2", "2", "1", "1", "1", "1", "1");
        assertEquals(a1, a2);
        assertEquals(a1.rest().rest().cons("2").cons("2"), a2);
    }

    @Test
    public void testCone() {
        ISeq a1 = List.neu("1", "1", "1", "1", "1");
        for (int i = 0; i < 5; i++) {
            a1 = a1.cone("2");
        }
        ISeq a2 = List.neu("1", "1", "1", "1", "1", "2", "2", "2", "2", "2");
        assertEquals(a1, a2);
        assertEquals(a1.rest().rest().cons("1").cons("1"), a2);
        assertEquals(a1.rest().rest().rest().rest().rest().cone("1").cone("1").cone("1").cone("1").cone("1").sorted(), a2);
    }
}