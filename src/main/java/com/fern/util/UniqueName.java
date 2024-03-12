package com.fern.util;

import java.util.Arrays;
import java.util.Iterator;

public final class UniqueName implements Iterable<String>, Iterator<String> {
    private static final String DEFAULT_NAME_SPACE = "dns_";
    private static final int DEFAULT_NUM_DIGITS = 5;
    private static final int GROW_BY_DIGITS = 2;
    private static final byte FIRST_ALPHA = (byte) 'a';
    private static final byte LAST_ALPHA = (byte) 'z';

    // singleton pattern
    private static final class SingletonInstantiate {
        static final UniqueName UNIQUE_INSTANCE = new UniqueName();
    }

    public static UniqueName get() {
        return SingletonInstantiate.UNIQUE_INSTANCE;
    }

    private final String nameSpace;
    private byte[] offsets;
    private int numDigits;
    private final StringBuilder sb;

    private UniqueName() {
        nameSpace = DEFAULT_NAME_SPACE;
        numDigits = DEFAULT_NUM_DIGITS;
        offsets = new byte[numDigits];
        Arrays.fill(offsets, FIRST_ALPHA);
        sb = new StringBuilder(numDigits * 2);
        sb.append(DEFAULT_NAME_SPACE);
    }

    @Override
    public synchronized String toString() {
        sb.setLength(nameSpace.length());
        for (int i = 0; i < numDigits; i++) {
            sb.append((char) offsets[i]);
        }
        return sb.toString();
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

    @Override
    public synchronized boolean hasNext() {
        return offsets[numDigits - 1] <= LAST_ALPHA;
    }

    @Override
    public synchronized String next() {
        if (hasNext()) {
            String name = toString();
            moveToNext(0);
            return name;
        }
        throw new IndexOutOfBoundsException();
    }

    private void moveToNext(int idx) {
        if (idx < numDigits) { // stop recursion otherwise
            offsets[idx]++;
            if (offsets[idx] > LAST_ALPHA) { // check overflow and perform carryover
                if (idx < numDigits - 1) {
                    // carry over is done if we have enough digits
                    offsets[idx] = FIRST_ALPHA;
                    moveToNext(idx + 1);
                } else {
                    // otherwise grow and reset
                    int newNumDigits = numDigits + GROW_BY_DIGITS;
                    byte[] newOffsets = new byte[newNumDigits];
                    System.arraycopy(offsets, 0, newOffsets, 0, numDigits);
                    Arrays.fill(newOffsets, FIRST_ALPHA);
                    offsets = newOffsets;
                    numDigits = newNumDigits;
                }
            }
        }
    }
}