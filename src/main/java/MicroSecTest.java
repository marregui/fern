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

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MicroSecTest {
    static final class Avg {
        private double sum = 0.0d;
        private int points = 0;

        void addPoint(long point) {
            sum += point;
            points++;
        }

        void reset() {
            sum = 0.0;
            points = 0;
        }

        double get() {
            return points != 0.0d ? sum / points : sum;
        }
    }

    static long timed(Function<int[], Void> fn, int[] array) {
        long start = System.nanoTime();
        try {
            fn.apply(array);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return TimeUnit.MICROSECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    static Void quicksort(int[] array) {
        quicksort(0, array.length - 1, array);
        return null;
    }

    static void quicksort(int low, int high, int[] array) {
        if (array == null || array.length == 0 || high <= low) {
            return;
        }
        int pivot = array[low + (high - low) / 2];
        int left = low;
        int right = high;
        while (left < right) {
            while (array[left] < pivot) {
                left++;
            }
            while (array[right] > pivot) {
                right--;
            }
            if (left <= right) {
                int t = array[left];
                array[left] = array[right];
                array[right] = t;
                left++;
                right--;
            }
        }
        if (left < high) {
            quicksort(left, high, array);
        }
        if (right > low) {
            quicksort(low, right, array);
        }
    }

    static Void bubblesort(int[] array) {
        boolean swapped = true;
        while (swapped) {
            swapped = false;
            for (int i = 1; i < array.length; i++) {
                if (array[i - 1] > array[i]) {
                    int tmp = array[i - 1];
                    array[i - 1] = array[i];
                    array[i] = tmp;
                    swapped = true;
                }
            }
        }
        return null;
    }

    static int[] genArray(int size, int min, int max) {
        int[] array = new int[size];
        Random rand = new Random();
        int upper = max - min + 1;
        for (int i = 0; i < size; i++) {
            array[i] = min + rand.nextInt(upper);
        }
        return array;
    }

    static void measure(Function<int[], Void> fn, int startArraySize, int incr) {
        int sampleSize = 2000;
        int arraySize = startArraySize;
        double time;
        Avg avg = new Avg();
        do {
            avg.reset();
            for (int i = 0; i < sampleSize; i++) {
                int[] array = genArray(arraySize, 0, 5 * arraySize);
                avg.addPoint(timed(fn, array));
            }
            time = avg.get();
            arraySize += incr;
            System.out.printf("Sample |%d|, array |%d| -> %.3f\n", sampleSize, arraySize, time);
        } while (100 - time >= 0.001);
        System.out.println("Thanks!");
    }

    public static void main(String[] args) {
        measure(MicroSecTest::quicksort, 1000, 50);
        measure(MicroSecTest::bubblesort, 100, 20);
    }
}