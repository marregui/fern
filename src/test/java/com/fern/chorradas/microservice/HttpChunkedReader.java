package com.fern.chorradas.microservice;

import com.fern.util.ILogger;
import com.fern.util.Logger;

import java.io.BufferedReader;

/**
 * Encapsulates the readout (slurp) of a whole response from an http based
 * server (which could be a heap killing chunk of text).
 *
 * @author Miguel Arregui (miguel.arregui@gmail.com)
 */
public final class HttpChunkedReader {
    private static final ILogger LOGGER = Logger.loggerFor(HttpChunkedReader.class);

    private enum SlurpStatus {
        ExpectingHTTPBegin,
        ReadingHeaders,
        ReadingSize,
        ReadingText,
        Completed
    }

    /**
     * @param br
     * @return The string contents arriving in the chunked http format
     * @throws Exception
     */
    public static String slurp(BufferedReader br) throws Exception {
        StringBuilder lines = new StringBuilder();
        int httpStatus;
        long nextChunkSize = -1;
        SlurpStatus slurpStatus = SlurpStatus.ExpectingHTTPBegin;
        loop:
        for (String line; null != (line = br.readLine()); ) {
            switch (slurpStatus) {
                case ExpectingHTTPBegin:
                    if (line.toUpperCase().startsWith("HTTP")) {
                        httpStatus = Integer.parseInt(line.split(" ")[1]);
                        slurpStatus = SlurpStatus.ReadingHeaders;
                        LOGGER.debug("Status: %d", httpStatus);
                    } else {
                        throw new IllegalStateException(String.format("[%s] unexpected:%s", slurpStatus, line));
                    }
                    break;

                case ReadingHeaders:
                    if (line.isEmpty()) {
                        slurpStatus = SlurpStatus.ReadingSize;
                        lines.setLength(0);
                        LOGGER.debug("End of HTTP headers");
                    } else {
                        String[] header = line.split(": ");
                        if (2 != header.length) {
                            throw new IllegalStateException(String.format("[%s] unexpected:%s", slurpStatus, line));
                        }
                        LOGGER.debug("Header [%s] -> %s", header[0].trim(), header[1].trim());
                    }
                    break;

                case ReadingSize:
                    try {
                        nextChunkSize = hexToDec(line);
                        LOGGER.debug("Size: %d (%s)", nextChunkSize, line);
                        if (0 == nextChunkSize) {
                            slurpStatus = SlurpStatus.Completed;
                            LOGGER.debug("EOF");
                        } else {
                            slurpStatus = SlurpStatus.ReadingText;
                        }
                    } catch (@SuppressWarnings("unused") Exception e) {
                        throw new IllegalStateException(String.format("[%s] unexpected:%s", slurpStatus, line));
                    }
                    break;

                case ReadingText:
                    if (nextChunkSize <= 0 || line.getBytes().length != nextChunkSize) {
                        throw new IllegalStateException(
                                String.format("[%s] size should be %d but is %d:%s", slurpStatus, nextChunkSize, line.length(), line));
                    }
                    lines.append(line);
                    slurpStatus = SlurpStatus.ReadingSize;
                    LOGGER.debug(line);
                    break;

                case Completed:
                    break loop;
            }
        }
        return lines.toString();
    }

    private static long hexToDec(String hex) {
        long dec = 0;
        long pow = 1L;
        for (int i = hex.length() - 1; i >= 0; i--, pow *= 16L) {
            dec += hexToDec(hex.charAt(i)) * pow;
        }
        return dec;
    }

    private static int hexToDec(char c) {
        char lc = Character.toLowerCase(c);
        if (lc >= 'a' && lc <= 'z') {
            return lc - 'a' + 10;
        } else if (lc >= '0' && lc <= '9') {
            return c - '0';
        }
        throw new IllegalArgumentException(String.format("Not a valid hex: %c", c));
    }

    private HttpChunkedReader() {
        throw new UnsupportedOperationException("no instances are allowed, it is a utilities class");
    }
}
