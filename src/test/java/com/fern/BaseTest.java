package com.fern;

import static com.fern.lang.Fn.defpred;
import static com.fern.util.Tools.str;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import com.fern.lang.FnBody;
import com.fern.lang.IPredicate;

public class BaseTest {
    /**
     * Predicate, checks that a number is even
     */
    public final static IPredicate even = defpred(new FnBody<>() {
        @Override
        public Boolean fnBody() {
            return (int) arg("$1") % 2 == 0;
        }
    });

    /**
     * Predicate, checks that a number is even
     */
    public final static IPredicate greater = defpred(new FnBody<Boolean>() {
        @Override
        public Boolean fnBody() {
            return (int) arg("$1") > (int) arg("$2");
        }
    });

    public static void print(final Object[] array) {
        if (array == null) {
            System.out.println("NULL");
        }
        final StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]).append(", ");
        }
        if (array.length > 0) {
            sb.setLength(sb.length() - 2);
        }
        System.out.println(sb.append("]").toString());
    }

    public static void print(final int[] array) {
        if (array == null) {
            System.out.println("NULL");
        }
        final StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]).append(", ");
        }
        if (array.length > 0) {
            sb.setLength(sb.length() - 2);
        }
        System.out.println(sb.append("]").toString());
    }

    /**
     * Runs the snippet expecting it to throw a specific exception
     * @param expectedExceptionClass Class of the Exception that should be thrown by running the snippet
     * @param snippet Snippet to be run
     */
    public static void expectFail(final Class<?> expectedExceptionClass, final Runnable snippet) {
        try {
            snippet.run();
        } catch (Exception e) {
            if (e.getClass() != expectedExceptionClass) {
                fail(str("Expected %s but got %s", expectedExceptionClass.getName(), e.getClass().getName()));
            }
            return;
        }
        fail(str("Expected %s but did not fail", expectedExceptionClass.getName()));
    }

    /**
     * @param snippet Snippet to be run and timed
     * @return Time it took to run the snippet in Microseconds (1e-6)
     */
    public static long timed(final Runnable snippet) {
        return timed(snippet, TimeUnit.MICROSECONDS);
    }

    /**
     * @param snippet Snippet to be run and timed
     * @param unit Units used for the return value
     * @return Time it took to run the snippet, in the specific time unit
     */
    public static long timed(final Runnable snippet, final TimeUnit unit) {
        final long start = System.nanoTime();
        long total = -1;
        try {
            snippet.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            total = unit.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
        return total;
    }

    /**
     * Helper class used to calculate running averages.
     * The average is calculated/returned for each point
     */
    public static final class Avg {
        private int points = 0;
        private double sum = 0.0;

        /**
         * Adds the point and calculates the new average
         * @param point
         * @return The current average
         */
        public double addPoint(long point) {
            this.sum += point;
            this.points++;
            return getAvg();
        }

        /**
         * @return The average for the given values/points added thus far
         */
        public double getAvg() {
            return this.points != 0 ? this.sum / this.points : this.sum;
        }
    }
}