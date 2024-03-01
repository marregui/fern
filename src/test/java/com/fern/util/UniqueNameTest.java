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
package com.fern.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class UniqueNameTest {
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    @Test
    public void testUniqueName() throws InterruptedException {
        UniqueName un = UniqueName.get();
        ConcurrentMap<String, String> names = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(() -> {
                while (un.hasNext()) {
                    String name = un.next();
                    String existingName = names.putIfAbsent(name, name);
                    if (existingName != null) {
                        Assert.fail("Name is not unique!");
                    }
                }
                Assert.fail("Should never happen!!!");
            });
        }
        System.out.printf("Running for 5 seconds\n");
        TimeUnit.SECONDS.sleep(5L);
        executor.shutdownNow();
        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
    }
}