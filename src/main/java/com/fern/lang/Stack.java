package com.fern.lang;

import com.fern.util.Util;

class Stack {
    private static final int INIT_SIZE = 20;
    private static final int GROWTH_FACTOR = 2;

    private Object[][] stack;
    private int offset;

    Stack() {
        this.stack = new Object[INIT_SIZE][];
        this.offset = 0;
    }

    synchronized int size() {
        return stack.length;
    }

    synchronized int offset() {
        return offset;
    }

    void push(Object[] e) {
        if (e == null) {
            throw new IllegalArgumentException("cannot push null");
        }
        synchronized (this) {
            if (offset == stack.length) {
                Object[][] newStack = new Object[stack.length * GROWTH_FACTOR][];
                System.arraycopy(stack, 0, newStack, 0, stack.length);
                stack = newStack;
            }
            stack[offset++] = e;
        }
    }

    Object[] replaceTop(Object[] e) {
        if (e == null) {
            throw new IllegalArgumentException("cannot replace with null");
        }
        Object[] prev = null;
        synchronized (this) {
            if (offset > 0) {
                prev = stack[offset - 1];
                stack[offset - 1] = e;
            }
        }
        return prev;
    }

    Object[] peek() {
        Object[] e = null;
        synchronized (this) {
            if (offset > 0) {
                e = stack[offset - 1];
            }
        }
        return e;
    }

    Object[] pop() {
        Object[] e = null;
        synchronized (this) {
            if (offset > 0) {
                e = stack[offset - 1];
                stack[offset - 1] = null;
                offset--;
            }
        }
        return e;
    }

    @Override
    public String toString() {
        StringBuilder sb = Util.THR_SB.get();
        sb.append("[<");
        synchronized (this) {
            sb.append(offset).append(">|");
            for (int i = 0; i < offset; i++) {
                Object[] e = stack[i];
                if (e != null) {
                    for (int j = 0; j < e.length; j++) {
                        sb.append(e[j]).append(",");
                    }
                    if (e.length > 0) {
                        sb.setLength(sb.length() - 1);
                    }
                } else {
                    sb.append("null");
                }
                sb.append("|");
            }
            if (offset > 0) {
                sb.setLength(sb.length() - 1);
            }
        }
        sb.append("]");
        return sb.toString();
    }
}