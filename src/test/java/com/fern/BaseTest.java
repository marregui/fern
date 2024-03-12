package com.fern;

import static com.fern.lang.Fn.defpred;
import static com.fern.util.Util.str;
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
    public final static IPredicate greater = defpred(new FnBody<>() {
        @Override
        public Boolean fnBody() {
            return (int) arg("$1") > (int) arg("$2");
        }
    });

    public static void print(Object[] array) {
        if (array == null) {
            System.out.println("NULL");
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]).append(", ");
        }
        if (array.length > 0) {
            sb.setLength(sb.length() - 2);
        }
        System.out.println(sb.append("]"));
    }

    public static void expectFail(Class<?> expectedExceptionClass, Runnable snippet) {
        try {
            snippet.run();
        } catch (Exception e) {
            if (e.getClass() != expectedExceptionClass) {
                fail(str("expected %s but got %s", expectedExceptionClass.getName(), e.getClass().getName()));
            }
            return;
        }
        fail(str("expected %s but did not fail", expectedExceptionClass.getName()));
    }

    /**
     * @param snippet Snippet to be run and timed
     * @return Time it took to run the snippet in Microseconds (1e-6)
     */
    public static long timed(Runnable snippet) {
        return timed(snippet, TimeUnit.MICROSECONDS);
    }

    /**
     * @param snippet Snippet to be run and timed
     * @param unit    Units used for the return value
     * @return Time it took to run the snippet, in the specific time unit
     */
    public static long timed(Runnable snippet, TimeUnit unit) {
        long start = System.nanoTime();
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

    public static final class Avg {
        private int points = 0;
        private double sum = 0.0;

        public void addPoint(long point) {
            sum += point;
            points++;
        }

        public double getAvg() {
            return points != 0 ? sum / points : sum;
        }
    }
}