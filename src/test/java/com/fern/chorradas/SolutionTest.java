package com.fern.chorradas;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class SolutionTest {

    private Solution.MinHeap h;

    @Before
    public void setUp() {
        h = new Solution.MinHeap();
    }

    @Test
    public void testAddRemove() {
        for (int i=10000; i > -1; i--) {
            h.add(i);
        }
        for (int expected=0, limit=h.size(); expected < limit; expected++) {
            int top = h.remove();
            Assert.assertEquals(expected, top);
        }
    }

    @Test
    public void testAddRemoveRandom() {
        Random rand = new Random();
        for (int i=10000; i > -1; i--) {
            h.add(rand.nextInt());
        }
        int min = Integer.MIN_VALUE;
        for (int i=0, limit=h.size(); i < limit; i++) {
            int top = h.remove();
            Assert.assertTrue(top >= min);
            min = top;
        }
    }

    @Test
    public void testAddRemoveValue() {
        h.add(1);
        h.add(4);
        h.add(9);
        h.add(17);
        h.add(-1);
        h.add(3);

        h.remove(4);

        Assert.assertEquals(-1, h.remove());
        Assert.assertEquals(1, h.remove());
        Assert.assertEquals(3, h.remove());
        Assert.assertEquals(9, h.remove());

        h.remove(17);

        Assert.assertEquals(h.size(), 0);

    }
}
