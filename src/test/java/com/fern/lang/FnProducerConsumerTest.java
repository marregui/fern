package com.fern.lang;

import static com.fern.lang.Fn.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.fern.lang.FnBody;
import com.fern.lang.IFn;

public class FnProducerConsumerTest {
    static final IFn<String> randStr = defn(defargs(Integer.class), new FnBody<String>() {
        @Override
        public String fnBody() {
            final Integer len = arg("$1");
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                final char c = (char) ('a' + ThreadLocalRandom.current().nextInt('z' - 'a' + 1));
                sb.append(c);
            }
            return sb.toString();
        }
    });

    static Runnable consumer(final int id, final BlockingQueue<String> queue, final long runningTimeMillis) {
        return defn(new FnBody<Void>() {
            @Override
            public Void fnBody() {
                return defn(defargs(Integer.class, BlockingQueue.class, Long.class), new FnBody<Void>() {
                    @Override
                    public Void fnBody() {
                        final Integer id = arg("$1");
                        final BlockingQueue<String> queue = arg("$2");
                        final Long duration = arg("$3");
                        final long end = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < end) {
                            String message = null;
                            try {
                                while ((message = queue.poll(200L, TimeUnit.MILLISECONDS)) == null) {
                                    // spin wait for messages to arrive
                                }
                            } catch (@SuppressWarnings("unused") InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            System.out.printf("Consumed <%d>: %s\n", id, message);
                        }
                        return null;
                    }
                }).invoke(id, queue, runningTimeMillis);
            }
        });
    }

    static Runnable producer(final int id, final BlockingQueue<String> queue, final long runningTimeMillis) {
        return defn(new FnBody<Void>() {
            @Override
            public Void fnBody() {
                return defn(defargs(Integer.class, BlockingQueue.class, Long.class), new FnBody<Void>() {
                    @Override
                    public Void fnBody() {
                        final Integer id = arg("$1");
                        final BlockingQueue<String> queue = arg("$2");
                        final Long duration = arg("$3");
                        final long end = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < end) {
                            final String message = randStr.invoke(100);
                            try {
                                while (false == queue.offer(message, 200L, TimeUnit.MILLISECONDS)) {
                                    // spin wait for the bus to be ready
                                }
                            } catch (@SuppressWarnings("unused") InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            System.out.printf("Produced <%d>: %s\n", id, message);
                        }
                        return null;
                    }
                }).invoke(id, queue, runningTimeMillis);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        final long runningTimeMillis = 5000L;
        final int queueSize = 5;
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(queueSize);
        final ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.submit(consumer(0, queue, runningTimeMillis));
        executor.submit(consumer(1, queue, runningTimeMillis));
        executor.submit(producer(0, queue, runningTimeMillis));
        executor.submit(producer(1, queue, runningTimeMillis));
        executor.submit(producer(2, queue, runningTimeMillis));
        TimeUnit.MILLISECONDS.sleep(runningTimeMillis);
        executor.shutdownNow();
        executor.awaitTermination(200L, TimeUnit.MILLISECONDS);
        queue.clear();
    }
}