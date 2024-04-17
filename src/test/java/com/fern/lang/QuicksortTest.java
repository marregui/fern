package com.fern.lang;

import static com.fern.lang.Fn.*;
import static com.fern.seq.Colls.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import com.fern.BaseTest;
import com.fern.lang.FnBody;
import com.fern.lang.IFn;
import com.fern.seq.ISeq;

public class QuicksortTest extends BaseTest {

    final static IPredicate lessThanPivot = defpred(new FnBody<Boolean>() {
        @Override
        public Boolean fnBody() {
            return (int) arg(1) < (int) arg(2);
        }
    });

    final static IPredicate greaterEqualThanPivot = defpred(new FnBody<Boolean>() {
        @Override
        public Boolean fnBody() {
            return (int) arg(1) >= (int) arg(2);
        }
    });

    final static IFn<ISeq> qsort = defn(defargs(ISeq.class), new FnBody<ISeq>() {
        @Override
        public ISeq fnBody() {
            final ISeq seq = arg(1);
            if (isNil(seq) || seq.isEmpty()) {
                return seq;
            }
            final int pivot = (int) seq.first();
            final ISeq rest = seq.rest();
            final ISeq lesser = filter(lessThanPivot, rest, pivot);
            final ISeq greater = filter(greaterEqualThanPivot, rest, pivot);
            return concat(qsort.invoke(lesser), pivot, qsort.invoke(greater));
        }
    });

    public static int[] quicksort(final int[] array) {
        quicksort(0, array.length - 1, array);
        return array;
    }

    public static void quicksort(final int low, final int high, final int[] array) {
        if (array == null || array.length == 0 || high <= low) {
            return;
        }
        int pivot = array[low];
        int i = low - 1;
        int j = high + 1;
        while (i < j) {
            do {
                i++;
            }
            while (i < high && array[i] < pivot);

            do {
                j--;
            }
            while (j > low && array[j] > pivot);

            if (i < j) {
                int t = array[i];
                array[i] = array[j];
                array[j] = t;
            }
        }
        if (low < j) {
            quicksort(low, j, array);
        }
        if (j + 1 < high) {
            quicksort(j + 1, high, array);
        }
    }

    static ISeq asList(final int[] array) {
        ISeq list = newlist();
        for (int e : array) {
            list = list.cone(e);
        }
        return list;
    }

    @Test
    public void testQuicksort() {

        assertEquals(qsort.invoke(newlist(3, 1, 6, 1, 0, 9, 2)), newlist(0, 1, 1, 2, 3, 6, 9));
        assertTrue(Arrays.equals(quicksort(new int[]{3, 1, 6, 1, 0, 9, 2}), new int[]{0, 1, 1, 2, 3, 6, 9}));
        assertTrue(Arrays.equals(quicksort(new int[]{1, 2, 11, 1}), new int[]{1, 1, 2, 11}));
        final int arraySize = 10000;
        final int[] array = genarray(arraySize, 0, arraySize);
        ISeq seq = asList(array.clone());
        assertEquals(asList(quicksort(array)), qsort.invoke(seq));
    }

    @Test
    public void testPerformanceBaseline() {
        final int sampleSize = 3;
        final int arraySize = 2000;
        final Avg classicImpl = new Avg();
        for (int i = 0; i < sampleSize; i++) {
            int[] array = genarray(arraySize, 0, arraySize);
            classicImpl.addPoint(timed(() -> quicksort(array)));
        }
        final Avg fernImpl = new Avg();
        for (int i = 0; i < sampleSize; i++) {
            ISeq seq = asList(genarray(arraySize, 0, arraySize));
            fernImpl.addPoint(timed(() -> qsort.invoke(seq)));
        }
        final double classic = classicImpl.getAvg();
        final double fern = fernImpl.getAvg();
        final double factor = fern / classic;
        System.out.printf("Sample |%d|, array |%d| classic -> %.3f, fern -> %.3f, factor -> %.3f\n",
                sampleSize, arraySize, classic, fern, factor);

        int[] array = genarray(arraySize, 0, arraySize);
        int[] array2 = new int[arraySize];
        System.arraycopy(array, 0, array2, 0, array.length);

        quicksort(array);
        quicksort(array2);
        int prev = array[0];
        for (int i = 1; i < array.length; i++) {
            Assert.assertEquals(prev, array2[i - 1]);
            Assert.assertTrue(prev <= array[i]);
            prev = array[i];
        }
        Assert.assertTrue(prev <= array[array.length - 1]);
        Assert.assertEquals(array[array.length - 1], array2[array2.length - 1]);
    }

    public static int[] genarray(final int size, final int min, final int max) {
        final int[] array = new int[size];
        final Random rand = new Random(0);
        final int upper = max - min + 1;
        for (int i = 0; i < size; i++) {
            array[i] = min + rand.nextInt(upper);
        }
        return array;
    }
}