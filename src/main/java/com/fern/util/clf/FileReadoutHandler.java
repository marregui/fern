package com.fern.util.clf;

import com.fern.util.ILogger;
import com.fern.util.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.channels.FileChannel.MapMode;

/**
 * This class defines a generic file readout process, whereby a file is
 * seen as a stream of '\n' separated chunks of bytes, which when interpreted
 * in UTF-8 encoding represent log lines of some format that encapsulates a
 * timestamp.
 * <p>
 * The readout method {@linkplain #fetchAvailableLines(ReadoutCache)}
 * reads all available lines in the file since the last time the method was called.
 * The file is read from 'fileReadOffset' to 'fileSize'.
 * <p>
 * Two methods are provided to change the offset the file is read from
 * {@linkplain #moveToStart()} and {@linkplain #moveToEnd()}.
 * <p>
 * Access to the file's contents is done through OS memory mapping, loading
 * to heap a line's worth of data to enable the parse through method
 * {@linkplain #parseLine(String)}.
 */
public abstract class FileReadoutHandler<LINE_TYPE extends WithUTCTimestamp> {

    private static final ILogger LOGGER = Logger.loggerFor(FileReadoutHandler.class);
    private static final String FILE_ACCESS_MODE = "r"; // read only
    private static final byte CARRIAGE_RETURN = '\r';
    private static final byte LINE_BREAK = '\n';
    private static final int LINE_BUFFER_SIZE = 512; // tune to average log size

    private final Path parentFolder;
    private final Path file;
    private long fileReadOffset;
    private byte[] lineBuffer;

    /**
     * Constructor.
     *
     * @param file source file
     */
    public FileReadoutHandler(Path file) {
        parentFolder = Objects.requireNonNull(file).getParent();
        this.file = file;
        lineBuffer = new byte[LINE_BUFFER_SIZE];
    }

    /**
     * Internally used to confirm the event source.
     *
     * @param other file to check
     * @return true if other(resolved by the parent folder path) is in fact the same
     * file being readout
     */
    boolean fileMatches(Path other) {
        return other != null && file.equals(parentFolder.resolve(other));
    }

    /**
     * @return the file
     */
    public Path getFile() {
        return file;
    }

    /**
     * @return the file's parent folder
     */
    public Path getParentFolder() {
        return parentFolder;
    }

    /**
     * @return current file's read offset, which will be at the start
     * of the next line to be parsed
     */
    public long getFileReadOffset() {
        return fileReadOffset;
    }

    /**
     * Moves the file's last read offset to the beginning (0L).
     */
    public void moveToStart() {
        fileReadOffset = 0L;
        LOGGER.debug("Moved to start at offset: {}", fileReadOffset);
    }

    /**
     * Moves the file's last read offset to the end (file size), if the
     * file exists, otherwise to offset 0L.
     *
     * @return true if the file exists
     */
    public boolean moveToEnd() {
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), FILE_ACCESS_MODE)) {
            fileReadOffset = raf.length();
            LOGGER.debug("Moved to end at offset: {}", fileReadOffset);
            return true;
        } catch (IOException e) {
            fileReadOffset = 0L;
            LOGGER.info("File does not exist yet, setting offset to 0L");
            return false;
        }
    }

    /**
     * Parsing method.
     * <p>
     * If it returns null, line will be parsed again in the next run of
     * {@linkplain #fetchAvailableLines(ReadoutCache)}.
     * <p>
     * If parsing should fail, an {@link IllegalArgumentException} is
     * expected is preferred to be thrown.
     *
     * @param line a UTF-8 String containing the last line read (without line separator chars)
     * @return an instance of type LINE_TYPE, or null (which means, 'return the line to the file')
     * @throws IllegalArgumentException when parsing fails
     */
    public abstract LINE_TYPE parseLine(String line);

    /**
     * This method maps the memory region corresponding to all unread bytes
     * (from the last read offset until the end of the file). This mapping is
     * split it into '\n' delimited blocks, which are UTF-8 decoded and fed
     * to a line parser one at the time. The result of this process is collected
     * into the readout cache.
     * <p>
     * Lines that fail to parse are ignored (a message is logged).
     * <p>
     * If the parser returns null, the process is stopped at the last successfully
     * parsed line. The next call of this method will resume from the 'failed' line.
     * This mechanism gives the parser the opportunity to throttle the readout
     * process.
     *
     * @param readoutCache readout cache where successfully parsed lines are added to
     * @return the number of lines added to the readout cache, which implies
     * successfully parsed
     * @throws IOException when the file cannot be read/mapped
     */
    public int fetchAvailableLines(ReadoutCache<LINE_TYPE> readoutCache) throws IOException {
        int addedLinesCount = 0;
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), FILE_ACCESS_MODE);
             FileChannel channel = raf.getChannel()) {
            long fileSize = raf.length();
            if (fileSize <= fileReadOffset) {
                fileReadOffset = fileSize;
                return addedLinesCount;
            }
            long bufferSize = fileSize - fileReadOffset;
            MappedByteBuffer mappedBuffer = channel.map(MapMode.READ_ONLY, fileReadOffset, bufferSize);
            int lineStartOffset = 0;
            LOGGER.debug("Reading {} additional bytes from offset {}",
                    bufferSize, fileReadOffset);
            for (int i = 0; i < mappedBuffer.limit(); i++) {
                if (mappedBuffer.get(i) == LINE_BREAK) {
                    if (lineStartOffset != i) {
                        int lineLength = i - lineStartOffset;
                        if (lineLength > lineBuffer.length) {
                            int newLineBufferSize = (int) Math.ceil(lineLength * 1.5f);
                            LOGGER.debug("Resizing buffer from {} to {}",
                                    lineBuffer.length,
                                    newLineBufferSize);
                            lineBuffer = new byte[newLineBufferSize];
                        }
                        mappedBuffer.position(lineStartOffset);
                        mappedBuffer.get(lineBuffer, 0, lineLength);
                        if (lineBuffer[lineLength - 1] == CARRIAGE_RETURN) {
                            lineLength--;
                        }
                        String line = new String(lineBuffer, 0, lineLength, StandardCharsets.UTF_8);
                        try {
                            LINE_TYPE parsed = parseLine(line);
                            if (parsed == null) {
                                LOGGER.debug("Interrupting readout, read null");
                                break;
                            } else {
                                readoutCache.add(parsed);
                                addedLinesCount++;
                            }
                        } catch (Exception e) {
                            LOGGER.warn("Ignoring malformed line found at offset {}: {}",
                                    fileReadOffset + lineStartOffset, line);
                        }
                    }
                    lineStartOffset = i + 1;
                }
            }
            fileReadOffset += lineStartOffset;
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("cannot access file: " + file, e);
        }
        if (addedLinesCount > 0) {
            LOGGER.debug("Loaded count: {}", addedLinesCount);
        }
        return addedLinesCount;
    }
}
