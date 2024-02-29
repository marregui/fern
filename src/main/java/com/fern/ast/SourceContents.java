/**
 * Copyright (c) Miguel Arregui. All rights reserved.
 * 
 * The use and distribution terms for this software are covered by the
 * 
 * Apache License 2.0
 * (https://opensource.org/licenses/Apache-2.0)
 * 
 * available in the LICENSE file at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound
 * by the terms of this license. You must not remove this notice, or
 * any other, from this software.
 **/
package com.fern.ast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public final class SourceContents {
  public static final SourceContents fromText(final String text) {
    return new SourceContents(null, text.toCharArray());
  }

  public static final SourceContents fromFile(final String fileName) throws Exception {
    final File file = new File(fileName);
    if (false == file.exists() || false == file.canRead()) {
      throw new Exception(String.format("Cannot read input file: %s", fileName));
    }
    final int fileLen = (int) file.length();
    try (InputStreamReader br = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
      final char[] cbuf = new char[fileLen];
      if (fileLen != br.read(cbuf, 0, fileLen)) {
        throw new Exception(String.format("Cannot read input file: %s", fileName));
      }
      return new SourceContents(file, cbuf);
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
      return i >= this.start && i < this.start + this.len;
    }

    @Override
    public String toString() {
      return String.format("Start:%d, End:%d, Len:%d", this.start, this.start + this.len - 1, this.len);
    }
  }

  private final File file;
  private final char[] cbuf;
  private final List<Range> lineRanges;
  private String contents;

  private SourceContents(final File file, final char[] cbuf) {
    this.file = file;
    this.cbuf = cbuf;
    this.lineRanges = new LinkedList<>();
    int lineStart = 0;
    for (int i=0; i < this.cbuf.length; i++) {
      final char c = this.cbuf[i];
      if ('\n' == c) {
        this.lineRanges.add(new Range(lineStart, i - lineStart + 1));
        lineStart = i + 1;
      }
    }
    if ('\n' != this.cbuf[this.cbuf.length - 1]) {
      this.lineRanges.add(new Range(lineStart, this.cbuf.length - lineStart));
    }
  }

  private final void checkLineRange(final int line) {
    if (line < 0 || line >= this.lineRanges.size()) {
      throw new IndexOutOfBoundsException(
          String.format("trying to access line %d when there are %d lines", line, this.lineRanges.size()));
    }
  }

  private final void checkFileOffset(final int offset) {
    if (offset < 0 || offset >= this.cbuf.length) {
      throw new IndexOutOfBoundsException(
          String.format("trying to access file offset %d when there are %d characters", offset, this.cbuf.length));
    }
  }

  public final int numberOfLines() {
    return this.lineRanges.size();
  }

  public final String lineContents(final int line) {
    checkLineRange(line);
    final Range range = this.lineRanges.get(line);
    return new String(this.cbuf, range.start, range.len);
  }

  public final int lineOffsetInFile(final int line) {
    checkLineRange(line);
    return this.lineRanges.get(line).start;
  }

  public final int numberOfCharsInLine(final int line) {
    checkLineRange(line);
    return this.lineRanges.get(line).len;
  }

  public final int lineNumberFor(final int fileOffset) {
    checkFileOffset(fileOffset);
    for (int line = 0; line < this.lineRanges.size(); line++) {
      final Range range = this.lineRanges.get(line);
      if (range.inRange(fileOffset)) {
        return line;
      }
    }
    return -1;
  }

  public final int charOffsetInLine(final int fileOffset) {
    checkFileOffset(fileOffset);
    int runningOffset = fileOffset;
    for (int line = 0; line < this.lineRanges.size(); line++) {
      final Range range = this.lineRanges.get(line);
      final int tmp = runningOffset - range.len;
      if (tmp >= 0) {
        runningOffset = tmp;
      }
      else {
        return runningOffset;
      }
    }
    return -1;
  }

  public final int length() {
    return this.cbuf.length;
  }

  public final char charAt(final int offset) {
    return this.cbuf[offset];
  }

  public final String substring(final int start, final int length) {
    return String.valueOf(this.cbuf, start, length);
  }

  public final synchronized String getContents() {
    if (null == this.contents) {
      this.contents = new String(this.cbuf, 0, this.cbuf.length);
    }
    return this.contents;
  }

  public final File getFile() {
    return this.file;
  }

  @Override
  public String toString() {
    return getContents();
  }
}