/**
 * Copyright (c) Miguel Arregui. All rights reserved.
 * <p>
 * The use and distribution terms for this software are covered by the
 * <p>
 * Apache License 2.0
 * (https://opensource.org/licenses/Apache-2.0)
 * <p>
 * available in the LICENSE file at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound
 * by the terms of this license. You must not remove this notice, or
 * any other, from this software.
 **/
package com.fern.lang;

import java.util.Map;
import java.util.LinkedHashMap;

import org.junit.Test;
import com.fern.BaseTest;
import com.fern.lang.Args;

import static org.junit.Assert.assertNotEquals;
import static com.fern.lang.Fn.defargs;
import static com.fern.lang.Fn.defvarargs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArgDefsTest extends BaseTest {
    @Test
    public void testConstructor() {
        expectFail(NullPointerException.class, () -> {
            defvarargs(null);
        });
        expectFail(NullPointerException.class, () -> {
            defvarargs(null, null);
        });
        final Args m = defvarargs(String.class, Integer.class);
        assertTrue(m.size() == 2);
        assertEquals(m.get(0), String.class);
        assertEquals(m.get(1), Integer.class);
        assertEquals(m, defvarargs(String.class, Integer.class));
        assertNotEquals(m, defargs(String.class, String.class));
        assertTrue(defargs().size() == 0);
    }

    @Test
    public void testFromBasic() {
        final Args m = defargs(Integer.class, String.class, Long.class, Character.class);
        final Args m2 = m.from(2);
        assertEquals(m2, defargs(Long.class, Character.class));
        assertFalse(m.isLastArgVararg());
        assertFalse(m2.isLastArgVararg());

        final Args m3 = defvarargs(Integer.class, String.class, Long.class, Character.class);
        final Args m4 = m3.from(2);
        assertEquals(m4, defvarargs(Long.class, Character.class));
        assertTrue(m3.isLastArgVararg());
        assertTrue(m4.isLastArgVararg());
    }

    @Test
    public void testFromIntense() {
        Args m = defargs(Integer.class, String.class);
        Args m2 = m.from(2);
        assertEquals(m.from(2), defargs());
        assertFalse(m.isLastArgVararg());
        assertFalse(m2.isLastArgVararg());

        Args m3 = defvarargs(Long.class, Character.class);
        Args m4 = m3.from(2);
        assertEquals(m4, defvarargs(Character.class));
        assertTrue(m3.isLastArgVararg());
        assertTrue(m4.isLastArgVararg());

        Args m5 = defvarargs(Long.class, Character.class);
        Args m6 = m5.from(4);
        assertEquals(m6, defvarargs(Character.class));
        assertTrue(m5.isLastArgVararg());
        assertTrue(m6.isLastArgVararg());
    }

    @Test
    public void findWhereArrayBecomesSlowerThanMapForWorseCase() {
        /* **
         * Answer: 40/50 entries; running through the array with a for loop is faster/equivalent
         * to using HashMap/LinkedHashMap when searching for the last entry (worse case scenario)
         */
        for (int n = 10; ; n += 10) {
            final String[] array = new String[n];
            final Map<String, Integer> map = new LinkedHashMap<>(n);
            for (int i = 0; i < n; i++) {
                map.put(array[i] = name(i), i);
            }
            final String findLastKey = name(n - 1);
            final int sampleSize = 10000;
            // warm up
            for (int i = 0; i < sampleSize; i++) {
                contains(array, findLastKey);
                contains(map, findLastKey);
            }
            final Avg mapAvg = new Avg();
            for (int i = 0; i < sampleSize; i++) {
                mapAvg.addPoint(timed(() -> contains(map, findLastKey)));
            }
            final Avg arrayAvg = new Avg();
            for (int i = 0; i < sampleSize; i++) {
                arrayAvg.addPoint(timed(() -> contains(array, findLastKey)));
            }
            final double aavg = arrayAvg.getAvg();
            final double mavg = mapAvg.getAvg();
            System.out.printf("N: %d, Sample |%d| array -> %.3f, map -> %.3f\n", n, sampleSize, aavg, mavg);
            if (aavg - mavg >= 0.01) {
                break;
            }
        }
    }

    private static String name(int i) {
        return String.format("Position: %d", i);
    }

    private static int contains(final Map<String, Integer> index, final String key) {
        final Integer i = index.get(key);
        return i != null ? i.intValue() : -1;
    }

    private static int contains(final String[] index, final String key) {
        for (int i = 0; i < index.length; i++) {
            if (index[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }
}