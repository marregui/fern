package com.fern.util;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;


public class UTCTimestampTest {

    @Test
    public void test_parse() throws ParseException {
        Assert.assertEquals(
                UTCTimestamp.parse("10/November/2020:16:00:00 +0000"),
                UTCTimestamp.parse("10/11/2020:16:00:00 +0000"));
    }

    @Test
    public void test_format() {
        Assert.assertEquals(
                UTCTimestamp.format(1604311200000L),
                "02/11/2020:10:00:00 +0000");
    }

    @Test
    public void test_formatForDisplay() {
        long ts = 1604311200000L;
        Assert.assertEquals(
                UTCTimestamp.formatForDisplay(ts),
                "02/11/2020:10:00:00 +0000 (" + ts + ")");
    }
}
