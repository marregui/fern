package com.fern.util.clf;

import com.fern.util.clf.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;


public class CacheTest {

    private List<CLF> logLines;

    @Before
    public void beforeEach() {
        logLines = Stream.of(
                        "127.0.0.1 - admin [10/11/2020:16:00:00 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:01 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:02 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:03 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020",
                        "127.0.0.1 - admin [10/11/2020:16:00:04 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020")
                .map(CLFParser::parseLogLine)
                .collect(Collectors.toList());
    }

    @Test
    public void test_cache_firstTimestamp_with_timestamp() throws ParseException {
        long ts = UTCTimestamp.parse("10/11/2020:16:00:03 +0000");
        ReadoutCache<CLF> cache = new ReadoutCache<>();
        cache.addAll(logLines);
        long next = cache.firstTimestampSince(ts);
        Assert.assertEquals(UTCTimestamp.format(next), "10/11/2020:16:00:04 +0000");
        ts = UTCTimestamp.parse("10/11/2020:16:00:04 +0000");
        Assert.assertEquals(cache.firstTimestampSince(ts), -1L);
    }

    @Test
    public void test_cache_fetch_followed_by_evict() {
        long ts0 = logLines.get(0).getUTCTimestamp();
        ReadoutCache<CLF> cache = new ReadoutCache<>();
        cache.addAll(logLines);
        List<CLF> cacheLine = cache.fetch(ts0, ts0);
        cache.evict(1);
        Assert.assertEquals(cacheLine.size(), 1);
        Assert.assertEquals(cacheLine.get(0).toString(),
                "127.0.0.1 - admin [10/11/2020:16:00:00 +0000] \"GET /resources/index.php HTTP/2.0\" 200 2020");
        logLines.stream()
                .map(clf -> UTCTimestamp.truncateMillis(clf.getUTCTimestamp()))
                .distinct()
                .skip(1)
                .forEach(ts -> {
                    List<CLF> lines = cache.fetch(ts, ts);
                    Assert.assertEquals(lines.size(), 10);
                    int cacheSize = cache.size();
                    cache.evict(lines.size());
                    Assert.assertEquals(cache.size(), cacheSize - lines.size());
                });
        Assert.assertEquals(cache.isEmpty(), true);
    }

    @Test
    public void test_slideBack() {
        int size = logLines.size();
        long ts = logLines.get(size >>> 1).getUTCTimestamp();
        int idx = ReadoutCache.findNearest(logLines, ts);
        Assert.assertEquals(idx, size >>> 1);
        idx = ReadoutCache.slideBack(logLines, idx);
        int i = 0;
        while (i < size && ts(logLines, i) != ts) {
            i++;
        }
        Assert.assertEquals(idx, i);
    }

    @Test
    public void test_slideForward() {
        int size = logLines.size();
        long ts = logLines.get(size >>> 1).getUTCTimestamp();
        int idx = ReadoutCache.findNearest(logLines, ts);
        Assert.assertEquals(idx, size >>> 1);
        idx = ReadoutCache.slideForward(logLines, idx);
        int i = size - 1;
        while (i >= 0 && ts(logLines, i) != ts) {
            i--;
        }
        Assert.assertEquals(idx, i);
    }

    @Test
    public void test_findNearest() {
        List<WithUTCTimestamp> timestamps = LongStream.iterate(System.currentTimeMillis(), l -> l + 2L)
                .mapToObj(l -> (WithUTCTimestamp) () -> l)
                .limit(4)
                .collect(Collectors.toList());
        for (int i = 0; i < timestamps.size(); i++) {
            long tsAtI = ts(timestamps, i);
            int idx = ReadoutCache.findNearest(timestamps, tsAtI + 1L);
            Assert.assertEquals(idx, i);
        }
        for (int i = timestamps.size() - 1; i > 0; i--) {
            long tsAtI = ts(timestamps, i);
            int idx = ReadoutCache.findNearest(timestamps, tsAtI - 1L);
            Assert.assertEquals(idx, i - 1);
        }
    }

    public static <T extends WithUTCTimestamp> long ts(List<T> entries, int idx) {
        return entries.get(idx).getUTCTimestamp();
    }

}
