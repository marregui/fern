package com.fern.util;


import java.nio.CharBuffer;

public final class TextSource extends Source {
    public static Source create(String text) {
        return new TextSource(text);
    }

    private final char[] cbuff;

    private TextSource(String text) {
        cbuff = text.toCharArray();
        findLineRanges(CharBuffer.wrap(cbuff));
    }

    @Override
    public String lineContent(int line) {
        checkLineBounds(line);
        int start = lineStartInFile(line);
        int len = lineLength(line);
        return new String(cbuff, start, len);
    }

    @Override
    public char charAt(int offset) {
        return cbuff[offset];
    }

    @Override
    public String substring(int start, int length) {
        return String.valueOf(cbuff, start, length);
    }

    @Override
    public String getContent() {
        return new String(cbuff);
    }

    @Override
    public String toString() {
        return getContent();
    }

    @Override
    public void close() {
        // no-op
    }
}