package com.fern.util;

import java.io.*;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileSourceTest {

    private static final String fileContent = """
            A father sees his son nearing manhood. What shall he tell that son?
            Life is hard; be steel; be a rock.
            And this might stand him for the storms
            and serve him for humdrum monotony
            and guide him among sudden betrayals
            and guide
            and tighten him for slack moments.
            """;

    @Test
    public void testFileSource() throws Exception {
        File file = saveToFile("tmp_file.txt", fileContent);
        file.deleteOnExit();
        try (Source reader = FileSource.create(file)) {
            checkContent(reader);
        }
    }

    @Test
    public void testTextSource() throws Exception {
        try (Source reader = TextSource.create(fileContent)) {
            checkContent(reader);
        }
    }

    private void checkContent(Source reader) {
        System.out.printf("Lines: %d, Chars: %d\n", reader.numberOfLines(), reader.length());
        for (int i = 0; i < reader.numberOfLines(); i++) {
            System.out.printf("Line %d [%d, %d, %d]: %s%n",
                    i,
                    reader.lineStartInFile(i),
                    reader.lineEndInFile(i),
                    reader.lineLength(i),
                    reader.lineContent(i));
        }

        checkLineNumberForFileOffset(reader, 35, 0);
        checkLineNumberForFileOffset(reader, 67, 0);
        checkLineNumberForFileOffset(reader, 68, 1);
        checkLineNumberForFileOffset(reader, 141, 2);
        checkLineNumberForFileOffset(reader, 143, 3);
        checkLineNumberForFileOffset(reader, 174, 3);
        checkLineNumberForFileOffset(reader, 200, 4);
        checkLineNumberForFileOffset(reader, 214, 4);
        checkLineNumberForFileOffset(reader, 215, 5);
        checkLineNumberForFileOffset(reader, 224, 5);
        checkLineNumberForFileOffset(reader, 225, 6);
        checkLineNumberForFileOffset(reader, 259, 6);
        checkLineNumberForFileOffset(reader, 260, -1);

        checkCharOffsetInLine(reader, 0, 0, 0);
        checkCharOffsetInLine(reader, 67, 0, 67);
        checkCharOffsetInLine(reader, 68, 1, 0);
        checkCharOffsetInLine(reader, 102, 1, 34);
        checkCharOffsetInLine(reader, 103, 2, 0);
        checkCharOffsetInLine(reader, 142, 2, 39);
        checkCharOffsetInLine(reader, 143, 3, 0);
        checkCharOffsetInLine(reader, 177, 3, 34);
        checkCharOffsetInLine(reader, 178, 4, 0);
        checkCharOffsetInLine(reader, 214, 4, 36);
        checkCharOffsetInLine(reader, 215, 5, 0);
        checkCharOffsetInLine(reader, 222, 5, 7);
        checkCharOffsetInLine(reader, 224, 5, 9);
        checkCharOffsetInLine(reader, 225, 6, 0);
        checkCharOffsetInLine(reader, 258, 6, 33);

        final int start = 215;
        final int end = 225;
        for (int i = 0; i < end - start; i++) {
            checkCharOffsetInLine(reader, start + i, 5, i);
        }
    }

    private static File saveToFile(String fileName, String fileContents) throws IOException {
        File file = new File(fileName);
        try (FileWriter out = new FileWriter(file)) {
            out.write(fileContents.toCharArray());
            return file;
        }
    }

    private static void checkLineNumberForFileOffset(Source reader, int offset, int expectedLine) {
        assertEquals(expectedLine, reader.lineNumber(offset));
    }

    private static void checkCharOffsetInLine(Source reader, int offset, int expectedLine, int expectedOffset) {
        assertEquals(expectedLine, reader.lineNumber(offset));
        assertEquals(expectedOffset, reader.charOffsetInLine(offset));
    }
}