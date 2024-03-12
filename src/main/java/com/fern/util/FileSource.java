package com.fern.util;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public final class FileSource extends Source {

    public static Source create(File file) {
        return new FileSource(file);
    }

    private final File file;
    private final RandomAccessFile raf;
    private final FileChannel channel;
    private final MappedByteBuffer mappedBuffer;

    private FileSource(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            throw new IllegalArgumentException(Util.str("cannot read input file: %s", file));
        }
        this.file = file;
        lineEndOffsets = new int[10];
        lineEndOffsetsLimit = 0;
        try {
            raf = new RandomAccessFile(file, "r");
            channel = raf.getChannel();
            mappedBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
        } catch (Throwable tr) {
            throw new RuntimeException(tr);
        }
        findLineRanges(mappedBuffer);
    }

    @Override
    public String lineContent(int line) {
        checkLineBounds(line);
        mappedBuffer.limit(lineEndInFile(line));
        mappedBuffer.position(lineStartInFile(line));
        return StandardCharsets.UTF_8.decode(mappedBuffer).toString();
    }

    @Override
    public char charAt(int offset) {
        checkFileBounds(offset);
        return mappedBuffer.getChar(offset);
    }

    @Override
    public String substring(int start, int length) {
        byte[] tmp = new byte[length];
        mappedBuffer.position(start);
        mappedBuffer.get(tmp);
        return new String(tmp, StandardCharsets.UTF_8);
    }

    @Override
    public String getContent() {
        mappedBuffer.position(0);
        return mappedBuffer.toString();
    }

    public File getFile() {
        return file;
    }

    @Override
    public void close() throws Exception {
        channel.close();
        raf.close();
        mappedBuffer.clear();
    }
}