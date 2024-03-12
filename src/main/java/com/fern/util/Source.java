package com.fern.util;


import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public abstract sealed class Source implements AutoCloseable
        permits FileSource, TextSource {

    protected int[] lineEndOffsets = new int[1 << 5];
    protected int lineEndOffsetsLimit;

    protected void checkFileBounds(int offset) {
        if (offset < 0 || lineEndOffsetsLimit == 0 || offset > lineEndOffsets[lineEndOffsetsLimit - 1]) {
            throw new IndexOutOfBoundsException(offset);
        }
    }

    protected void checkLineBounds(int line) {
        if (line < 0 || line >= lineEndOffsetsLimit) {
            throw new IndexOutOfBoundsException(line);
        }
    }

    protected int lineStartInFile(int line) {
        return line > 0 ? lineEndOffsets[line - 1] + 1 : 0;
    }

    protected int lineEndInFile(int line) {
        return line > -1 ? lineEndOffsets[line] : 0;
    }

    protected int lineLength(int line) {
        return line > 0 ? lineEndOffsets[line] - lineEndOffsets[line - 1] - 1 : lineEndOffsets[line];
    }

    protected int numberOfLines() {
        return lineEndOffsetsLimit;
    }

    protected abstract String lineContent(int line);

    protected int lineNumber(int fileOffset) {
        if (fileOffset > -1 && fileOffset <= lineEndOffsets[lineEndOffsetsLimit - 1]) {
            int left = 0;
            int right = lineEndOffsetsLimit - 1;
            int pivot;
            int limit;
            while (left < right) {
                pivot = (right + left) >> 1;
                limit = lineEndOffsets[pivot];
                if (fileOffset < limit) {
                    right = pivot - 1;
                } else if (fileOffset > limit) {
                    left = pivot + 1;
                } else {
                    return pivot;
                }
            }
            if (left == right) {
                return fileOffset <= lineEndOffsets[left] ? left : left + 1;
            }
        }
        return -1;
    }

    protected int charOffsetInLine(int fileOffset) {
        if (fileOffset > -1 && fileOffset <= lineEndOffsets[lineEndOffsetsLimit - 1]) {
            int left = 0;
            int right = lineEndOffsetsLimit - 1;
            int pivot;
            int limit;
            while (left < right) {
                pivot = (right + left) >> 1;
                limit = lineEndOffsets[pivot];
                if (fileOffset < limit) {
                    right = pivot - 1;
                } else if (fileOffset > limit) {
                    left = pivot + 1;
                } else if (pivot == 0) {
                    return fileOffset;
                } else {
                    return fileOffset - lineEndOffsets[pivot - 1] - 1;
                }
            }
            if (left == right) {
                limit = lineEndOffsets[left];
                if (fileOffset > limit) {
                    return fileOffset - limit - 1;
                }
                return left != 0 ? fileOffset - lineEndOffsets[left - 1] - 1 : fileOffset;
            }
        }
        return -1;
    }

    protected int length() {
        return lineEndOffsetsLimit > 0 ? lineEndOffsets[lineEndOffsetsLimit - 1] : 0;
    }

    protected abstract char charAt(int offset);

    protected abstract String substring(int start, int length);

    public abstract String getContent();

    protected void findLineRanges(ByteBuffer content) {
        lineEndOffsetsLimit = 0;
        int limit = content.limit();
        for (int i = 0; i < limit; i++) {
            if ('\n' == content.get(i)) {
                addRange(i);
            }
        }
        if ('\n' != content.get(limit - 1)) {
            addRange(limit - 1);
        }
    }

    protected void findLineRanges(CharBuffer content) {
        lineEndOffsetsLimit = 0;
        int limit = content.limit();
        for (int i = 0; i < limit; i++) {
            if ('\n' == content.get(i)) {
                addRange(i);
            }
        }
        if ('\n' != content.get(limit - 1)) {
            addRange(limit - 1);
        }
    }

    protected File getFile() {
        return null;
    }

    private void addRange(int end) {
        if (lineEndOffsetsLimit >= lineEndOffsets.length) {
            int[] tmp = new int[lineEndOffsets.length << 1];
            System.arraycopy(lineEndOffsets, 0, tmp, 0, lineEndOffsets.length);
            lineEndOffsets = tmp;
        }
        lineEndOffsets[lineEndOffsetsLimit++] = end;
    }
}
