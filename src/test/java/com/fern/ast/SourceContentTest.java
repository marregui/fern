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
import java.io.PrintStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SourceContentTest {
    @Test
    public void readFile() throws Exception {
        final String fileName = "nabiscopicopoki";
        String fileContents = "A father sees his son nearing manhood. What shall he tell that son?\n";
        fileContents += "Life is hard; be steel; be a rock.\n";
        fileContents += "And this might stand him for the storms\n";
        fileContents += "and serve him for humdrum monotony\n";
        fileContents += "and guide him among sudden betrayals\n";
        fileContents += "and guide\n";
        fileContents += "and tighten him for slack moments.";
        final File file = saveToFile(fileName, fileContents);
        final SourceContent reader = SourceContent.fromFile(fileName);
        final String contentsFromReader = reader.getContent();
        System.out.printf("Lines: %d, Chars: %d\n", reader.numberOfLines(), reader.length());
        for (int i = 0; i < reader.numberOfLines(); i++) {
            System.out.printf("Line %d [%d, %d, %d]: %s",
                    i,
                    reader.lineOffsetInFile(i),
                    reader.lineOffsetInFile(i) + reader.numberOfCharsInLine(i) - 1, reader.numberOfCharsInLine(i),
                    reader.lineContent(i));
        }
        System.out.println("");
        checkLineNumberForFileOffset(reader, 35, 0);
        checkLineNumberForFileOffset(reader, 68, 1);
        checkLineNumberForFileOffset(reader, 141, 2);
        checkLineNumberForFileOffset(reader, 174, 3);
        checkLineNumberForFileOffset(reader, 200, 4);
        checkLineNumberForFileOffset(reader, 214, 4);
        checkLineNumberForFileOffset(reader, 215, 5);
        checkLineNumberForFileOffset(reader, 225, 6);
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
        assertEquals(fileContents, contentsFromReader);
        file.delete();
    }

    private static final File saveToFile(final String fileName, final String fileContents) {
        final File file = new File(fileName);
        try (PrintStream out = new PrintStream(file)) {
            out.print(fileContents);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final void checkLineNumberForFileOffset(SourceContent reader, int offset, int expectedLine) {
        assertTrue(expectedLine == reader.lineNumberFor(offset));
    }

    private static final void checkCharOffsetInLine(SourceContent reader, int offset, int expectedLine, int expectedOffset) {
        assertTrue(expectedLine == reader.lineNumberFor(offset));
        assertTrue(expectedOffset == reader.charOffsetInLine(offset));
    }
}