package com.fern.util.clf;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.fern.util.Store;
import org.junit.Assert;
import org.junit.Test;

public class FileReadoutHandlerTest {

    @Test
    public void test_concurrent_writer_and_reader_agree_on_file_contents_and_interpretation() throws InterruptedException, IOException {
        Path file = Store.resolve(Store.accessLogFileName());
        AtomicLong totalLogLinesRead = new AtomicLong();
        AtomicLong totalLogLinesWritten = new AtomicLong();
        CountDownLatch dataAvailable = new CountDownLatch(1);
        long maxLogs = 15_000L;
        long width = 25 * 1000L;
        long delta = 1L;

        Thread logLinesProducer = new Thread(() -> {
            long start = System.currentTimeMillis();
            for (long i = 0; !Thread.currentThread().isInterrupted(); i++) {
                try {
                    long count = Store.storeToFile(file, new CLFGenerator(start, width / 5, delta), i > 0);
                    if (dataAvailable.getCount() > 0) {
                        dataAvailable.countDown();
                    }
                    if (totalLogLinesWritten.addAndGet(count) >= maxLogs) {
                        break;
                    }
                    start = start + width;
                } catch (IOException e) {
                    break;
                }
            }
        }, "logLinesProducer");

        ReadoutCache<CLF> loadedLogLines = new ReadoutCache<>();
        Thread logLinesConsumer = new Thread(() -> {
            FileReadoutHandler<CLF> readoutHandler = new CLFReadoutHandler(file);
            int consecutiveNothingReadCount = 0;
            try {
                dataAvailable.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int linesCount = readoutHandler.fetchAvailableLines(loadedLogLines);
                    if (linesCount > 0) {
                        totalLogLinesRead.addAndGet(linesCount);
                        consecutiveNothingReadCount = 0;
                    } else {
                        consecutiveNothingReadCount++;
                        try {
                            TimeUnit.MILLISECONDS.sleep(100L);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        if (consecutiveNothingReadCount > 10) {
                            break;
                        }
                    }
                } catch (IllegalStateException e) {
                    // does not exist yet
                } catch (IOException e) {
                    break;
                }
            }
        }, "logLinesConsumer");

        try {
            long startTs = System.currentTimeMillis();
            logLinesProducer.start();
            logLinesConsumer.start();
            logLinesProducer.join();
            logLinesConsumer.join();
            long elapsedTs = System.currentTimeMillis() - startTs;
            System.out.println("Total lines written: " + totalLogLinesWritten);
            System.out.println("Total lines read: " + totalLogLinesRead);
            System.out.println("Total millis: " + elapsedTs);
            Assert.assertEquals(totalLogLinesWritten.get(), totalLogLinesRead.get());
            Assert.assertEquals(totalLogLinesRead.get(), maxLogs);
            ReadoutCache<CLF> readoutCache = new ReadoutCache<>();
            int fileSize = new CLFReadoutHandler(file).fetchAvailableLines(readoutCache);
            Assert.assertEquals(fileSize, loadedLogLines.size());
            Assert.assertEquals(fileSize, (int) maxLogs);
            Assert.assertEquals(fileSize, readoutCache.size());
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
