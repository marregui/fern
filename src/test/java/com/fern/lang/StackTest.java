package com.fern.lang;

import com.fern.BaseTest;
import com.fern.lang.Stack;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;

public class StackTest extends BaseTest {
    @Test
    public void testGrowth() {
        final Stack stack = new Stack();
        final int size = stack.size();
        for (int i = 0; i < size; i++) {
            final Object[] e = new Object[i];
            stack.push(e);
            assertTrue(Arrays.equals(stack.peek(), e));
            assertTrue(stack.offset() == i + 1);
        }
        assertTrue(stack.offset() == size);
        assertTrue(stack.size() == size);
        final Object[] e = new Object[size];
        stack.push(e);
        assertTrue(Arrays.equals(stack.peek(), e));
        assertTrue(stack.offset() == size + 1);
        assertFalse(stack.size() == size);
        final int total = stack.offset();
        for (int i = 0; i < total; i++) {
            assertTrue(Arrays.equals(stack.pop(), new Object[total - i - 1]));
        }
        assertTrue(stack.offset() == 0);
        assertNull(stack.peek());
    }

    @Test
    public void testReplace() {
        final Stack stack = new Stack();
        final Object[] init = new String[]{"top", "of", "the", "stack"};
        final Object[] replacement = new Integer[]{1, 2, 3, 4, 5};
        stack.push(init);
        assertTrue(stack.offset() == 1);
        assertTrue(Arrays.equals(stack.peek(), init));
        final Object[] replaced = stack.replaceTop(replacement);
        assertTrue(stack.offset() == 1);
        assertTrue(Arrays.equals(stack.peek(), replacement));
        assertTrue(replaced == init);
        assertTrue(Arrays.equals(replaced, init));
    }
}