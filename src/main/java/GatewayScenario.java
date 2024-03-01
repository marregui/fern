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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatewayScenario {
    static abstract class Agent implements Runnable {
        static final AtomicLong UNIQUE_MESSAGE_ID = new AtomicLong();
        static final Pattern MESSAGE_PARSER = Pattern.compile("\\{:id (\\d+) :cargo ([^\\}]+)\\}");

        static String formatOutboundMessage(String message) {
            return String.format("{:id %d :cargo %s}", UNIQUE_MESSAGE_ID.getAndIncrement(), message);
        }

        static String parseInboundMessage(String message) {
            Matcher m = MESSAGE_PARSER.matcher(message);
            if (m.find() && m.groupCount() == 2) {
                String cargo = m.group(2);
                return cargo;
            }
            throw new IllegalStateException("should never happen");
        }

        final BlockingQueue<String> messageBus;
        final AtomicBoolean isActive;
        final long runningTimeMillis;
        final AtomicLong scheduled;

        Agent(BlockingQueue<String> messageBus, long runningTimeMillis) {
            this.messageBus = messageBus;
            this.isActive = new AtomicBoolean(true);
            this.runningTimeMillis = runningTimeMillis;
            this.scheduled = new AtomicLong(System.currentTimeMillis() + runningTimeMillis);
        }

        @Override
        public final void run() {
            isActive.set(true);
            scheduled.set(System.currentTimeMillis() + runningTimeMillis);
            try {
                while (isActive.get() && System.currentTimeMillis() < scheduled.get()) {
                    doAction();
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        abstract void doAction();

        void send(String message) {
            String wireMessage = formatOutboundMessage(message);
            while (false == messageBus.offer(wireMessage)) {
                // spin wait for the bus to be ready
            }
            System.out.printf("<OUT>: %s\n", wireMessage);
        }

        String receive() {
            String wireMessage = null;
            while ((wireMessage = messageBus.poll()) == null) {
                // spin wait for messages to arrive
            }
            System.out.printf("<IN>: %s\n", wireMessage);
            return parseInboundMessage(wireMessage);
        }
    }

    static final int WHOLE_PART_LENGTH = 5;
    static final int SIGNIFICANT_DECIMALS = 8;
    static final String DBL_TO_STR_FMT = String.format("%%.%df", SIGNIFICANT_DECIMALS);
    static final int MESSAGE_BUS_BANDWIDTH = 12;

    static final double randomDouble() {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < WHOLE_PART_LENGTH + SIGNIFICANT_DECIMALS; j++) { // (2^63 - 1 = 9.223372036854776E18)
            sb.append(ThreadLocalRandom.current().nextInt(10));
            if (j == WHOLE_PART_LENGTH - 1) {
                sb.append(".");
            }
        }
        return Double.parseDouble(sb.toString());
    }

    static String dblToWireFormat(double qty) {
        return String.format(DBL_TO_STR_FMT, qty);
    }

    static double dblFromWireFormat(String qtyStr) {
        return Double.parseDouble(qtyStr);
    }

    static class Market extends Agent {
        Market(BlockingQueue<String> queue, long runningTimeMillis) {
            super(queue, runningTimeMillis);
        }

        @Override
        void doAction() {
            double realQty = randomDouble();
            String wireQty = dblToWireFormat(realQty);
            send(wireQty);
            System.out.printf("Sent: %f as %s\n", realQty, wireQty);
        }
    }

    static class Consumer extends Agent {
        Consumer(BlockingQueue<String> queue, long runningTimeMillis) {
            super(queue, runningTimeMillis);
        }

        @Override
        void doAction() {
            String wireQty = receive();
            double qty = dblFromWireFormat(wireQty);
            System.out.printf("Received: %s as %f}\n", wireQty, qty);
        }
    }

    final BlockingQueue<String> messageBus;
    final ExecutorService executor;

    GatewayScenario() {
        messageBus = new ArrayBlockingQueue<>(MESSAGE_BUS_BANDWIDTH);
        executor = Executors.newFixedThreadPool(2);
    }

    void runFor(final long runningTimeMillis) throws InterruptedException {
        executor.submit(new Consumer(messageBus, runningTimeMillis));
        executor.submit(new Market(messageBus, runningTimeMillis));
        TimeUnit.MILLISECONDS.sleep(runningTimeMillis);
        executor.shutdownNow();
        executor.awaitTermination(200L, TimeUnit.MILLISECONDS);
        messageBus.clear();
    }

    public static void main(String[] args) throws Exception {
        new GatewayScenario().runFor(5000L);
    }
}