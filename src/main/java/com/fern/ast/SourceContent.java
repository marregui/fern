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
package com.fern.ast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public final class SourceContent {
    public static SourceContent fromText(String text) {
        return new SourceContent(null, text.toCharArray());
    }

    public static SourceContent fromFile(String fileName) throws Exception {
        File file = new File(fileName);
        if (false == file.exists() || false == file.canRead()) {
            throw new Exception(String.format("Cannot read input file: %s", fileName));
        }
        int fileLen = (int) file.length();
        try (InputStreamReader br = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            char[] cbuf = new char[fileLen];
            if (fileLen != br.read(cbuf, 0, fileLen)) {
                throw new Exception(String.format("Cannot read input file: %s", fileName));
            }
            return new SourceContent(file, cbuf);
        }
    }

    private static final class Range {
        public final int start;
        public final int len;

        public Range(int start, int len) {
            this.start = start;
            this.len = len;
        }

        public boolean inRange(int i) {
            return i >= start && i < start + len;
        }

        @Override
        public String toString() {
            return String.format("Start:%d, End:%d, Len:%d", start, start + len - 1, len);
        }
    }

    private final File file;
    private final char[] cbuf;
    private final List<Range> lineRanges;
    private String content;

    private SourceContent(File file, char[] cbuf) {
        this.file = file;
        this.cbuf = cbuf;
        this.lineRanges = new LinkedList<>();
        int lineStart = 0;
        for (int i = 0; i < this.cbuf.length; i++) {
            char c = this.cbuf[i];
            if ('\n' == c) {
                this.lineRanges.add(new Range(lineStart, i - lineStart + 1));
                lineStart = i + 1;
            }
        }
        if ('\n' != this.cbuf[this.cbuf.length - 1]) {
            this.lineRanges.add(new Range(lineStart, this.cbuf.length - lineStart));
        }
    }

    private void checkLineRange(int line) {
        if (line < 0 || line >= lineRanges.size()) {
            throw new IndexOutOfBoundsException(
                    String.format("trying to access line %d when there are %d lines", line, lineRanges.size()));
        }
    }

    private void checkFileOffset(int offset) {
        if (offset < 0 || offset >= cbuf.length) {
            throw new IndexOutOfBoundsException(
                    String.format("trying to access file offset %d when there are %d characters", offset, cbuf.length));
        }
    }

    public int numberOfLines() {
        return lineRanges.size();
    }

    public String lineContent(int line) {
        checkLineRange(line);
        Range range = lineRanges.get(line);
        return new String(cbuf, range.start, range.len);
    }

    public int lineOffsetInFile(int line) {
        checkLineRange(line);
        return lineRanges.get(line).start;
    }

    public int numberOfCharsInLine(int line) {
        checkLineRange(line);
        return lineRanges.get(line).len;
    }

    public int lineNumberFor(int fileOffset) {
        checkFileOffset(fileOffset);
        for (int line = 0; line < lineRanges.size(); line++) {
            Range range = lineRanges.get(line);
            if (range.inRange(fileOffset)) {
                return line;
            }
        }
        return -1;
    }

    public int charOffsetInLine(int fileOffset) {
        checkFileOffset(fileOffset);
        int runningOffset = fileOffset;
        for (int line = 0; line < lineRanges.size(); line++) {
            Range range = lineRanges.get(line);
            int tmp = runningOffset - range.len;
            if (tmp >= 0) {
                runningOffset = tmp;
            } else {
                return runningOffset;
            }
        }
        return -1;
    }

    public int length() {
        return cbuf.length;
    }

    public char charAt(int offset) {
        return cbuf[offset];
    }

    public String substring(int start, int length) {
        return String.valueOf(cbuf, start, length);
    }

    public synchronized String getContent() {
        if (null == content) {
            content = new String(cbuf);
        }
        return content;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return getContent();
    }
}