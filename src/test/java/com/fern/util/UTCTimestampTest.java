/* **
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2020, Miguel Arregui a.k.a. marregui
 */

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
