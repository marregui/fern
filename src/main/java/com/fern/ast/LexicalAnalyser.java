package com.fern.ast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fern.ast.Symbol.SymbolType;

public final class LexicalAnalyser implements Iterable<Symbol>, Iterator<Symbol> {
    private static final String NOT_FROM_FILE = "NOT FROM FILE";

    public static final char OPEN_P_CHAR = '(';
    public static final char CLOSE_P_CHAR = ')';
    public static final char OPEN_B_CHAR = '[';
    public static final char CLOSE_B_CHAR = ']';

    private static final char NEWLINE_CHAR = '\n';
    private static final char COMMENT_CHAR = ';';
    private static final char SPACE_CHAR = ' ';
    private static final char STRING_CHAR = '"';

    private static final char MINUS_CHAR = '-';
    private static final char PLUS_CHAR = '+';
    private static final char PROD_CHAR = '*';
    private static final char DIV_CHAR = '/';
    private static final char MOD_CHAR = '%';
    private static final char DOT_CHAR = '.';
    private static final char AND_CHAR = '&';
    private static final char OR_CHAR = '|';
    private static final char GT_CHAR = '>';
    private static final char LT_CHAR = '<';
    private static final char EQ_CHAR = '=';
    private static final char NOT_CHAR = '!';
    private static final Set<Character> FIRST_OF_OPERATORS = new HashSet<>(Arrays.asList(
            MINUS_CHAR,
            PLUS_CHAR,
            PROD_CHAR,
            DIV_CHAR,
            MOD_CHAR,
            AND_CHAR,
            OR_CHAR,
            LT_CHAR,
            GT_CHAR,
            EQ_CHAR,
            NOT_CHAR,
            DOT_CHAR
    ));

    private static final Set<String> KEYWORDS;

    static {
        Set<String> keywords = new HashSet<>();
        for (Symbol.Keyword k : Symbol.Keyword.values()) {
            keywords.add(k.key);
        }
        KEYWORDS = Collections.unmodifiableSet(keywords);
    }

    private static boolean isValidRefOrKeywordStart(char c) {
        return '_' == c || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isValidRefOrKeywordChar(char c) {
        return '_' == c || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

    /**
     * Factory method
     *
     * @param reader
     * @return A LexicalAnalyser from the SourceContents
     */
    public static LexicalAnalyser get(SourceContent reader) {
        return new LexicalAnalyser(reader);
    }

    /**
     * Factory method that uses an internal LexicalAnalyser on the text
     * to get a list of Symbols
     *
     * @param text
     * @return The list of Symbols from the text
     */
    public static List<Symbol> getSymbols(String text) {
        return new LexicalAnalyser(SourceContent.fromText(text)).analyse().getSymbols();
    }

    public static void spit(String text) {
        for (Symbol sym : getSymbols(text)) {
            System.out.println(sym);
        }
    }

    private final SourceContent reader;
    private final List<Symbol> symbols;
    private int currentSymbolOffset;

    private LexicalAnalyser(SourceContent reader) {
        this.reader = reader;
        this.symbols = new ArrayList<>();
    }

    /**
     * Performs the lexical analysis from scratch
     */
    public LexicalAnalyser analyse() {
        symbols.clear();
        currentSymbolOffset = 0;
        final int limit = reader.length();
        for (int i = 0; i < limit; i++) {
            char c = reader.charAt(i);

            // whites
            if (Character.isWhitespace(c)) {
                continue;
            }

            // comments
            if (COMMENT_CHAR == c) {
                // consume the comment prefix
                i++;
                while (i < limit && ((c = reader.charAt(i)) == COMMENT_CHAR || c == SPACE_CHAR)) {
                    i++;
                }
                int commentStart = i;
                while (i < limit && (c = reader.charAt(i)) != COMMENT_CHAR && c != NEWLINE_CHAR) {
                    i++;
                }
                int commentLength = i - commentStart;
                String comment = commentLength == 0 ? "" : reader.substring(commentStart, commentLength);
                int line = reader.lineNumberFor(commentStart);
                int offset = reader.charOffsetInLine(commentStart);
                symbols.add(Symbol.get(comment, Symbol.SymbolType.COMMENT, line, offset));
                continue;
            }

            // string
            if (STRING_CHAR == c) {
                i++; // consume string start char
                int stringStart = i;
                while (i < limit && (c = reader.charAt(i)) != STRING_CHAR) {
                    i++;
                }
                if (i >= limit && c != STRING_CHAR) {
                    throw new RuntimeException(String.format(
                            "string starting with %c at offset %d has not been closed",
                            STRING_CHAR, stringStart
                    ));
                }
                int stringLength = i - stringStart;
                String string = stringLength == 0 ? "" : reader.substring(stringStart, stringLength);
                int line = reader.lineNumberFor(stringStart);
                int offset = reader.charOffsetInLine(stringStart);
                symbols.add(Symbol.get(string, Symbol.SymbolType.STRING, line, offset));
                continue;
            }

            // number: [+-]?d+(.|.d+)?([eE][+-]?d+)?
            if (PLUS_CHAR == c || MINUS_CHAR == c || Character.isDigit(c)) {
                int numberStart = i;
                boolean hasOptionalDecimalPart = false;
                boolean hasOptionalScientificNotationPart = false;

                // check whether the sign is really a sign or an operator
                if (PLUS_CHAR == c || MINUS_CHAR == c) {
                    if (i + 1 < limit) {
                        if (false == Character.isDigit(reader.charAt(i + 1))) {
                            // signs must be immediately next to a digit to be considered a sign
                            int line = reader.lineNumberFor(i);
                            int offset = reader.charOffsetInLine(i);
                            symbols.add(Symbol.get(String.valueOf(c), Symbol.SymbolType.ARITHMETIC_OP, line, offset));
                            continue;
                        }
                    } else {
                        // We found the end of the file at a sign, so it becomes an operator
                        int line = reader.lineNumberFor(i);
                        int offset = reader.charOffsetInLine(i);
                        symbols.add(Symbol.get(String.valueOf(c), Symbol.SymbolType.ARITHMETIC_OP, line, offset));
                        continue;
                    }
                }

                i++; // skip either sign or first digit

                // skip all digits
                while (i < limit && Character.isDigit(reader.charAt(i))) {
                    i++;
                }

                // if we found the end of the file we are done
                if (i >= limit) {
                    String number = reader.substring(numberStart, i - numberStart);
                    int line = reader.lineNumberFor(numberStart);
                    int offset = reader.charOffsetInLine(numberStart);
                    symbols.add(Symbol.get(number, Symbol.SymbolType.INTEGER, line, offset));
                    continue;
                }

                // number: [+-]?d+(.|.d+)?([eE][+-]?d+)?
                // optional decimal part: (.|.d+)?
                char cc = reader.charAt(i);
                if (DOT_CHAR == cc) {
                    i++; // skip dot

                    if (i >= limit) {
                        String number = reader.substring(numberStart, i - numberStart);
                        int line = reader.lineNumberFor(numberStart);
                        int offset = reader.charOffsetInLine(numberStart);
                        symbols.add(Symbol.get(number, Symbol.SymbolType.FLOATING, line, offset));
                        continue;
                    }

                    // skip digits
                    while (i < limit && Character.isDigit(reader.charAt(i))) {
                        i++;
                    }

                    if (i >= limit) {
                        String number = reader.substring(numberStart, i - numberStart);
                        int line = reader.lineNumberFor(numberStart);
                        int offset = reader.charOffsetInLine(numberStart);
                        symbols.add(Symbol.get(number, Symbol.SymbolType.FLOATING, line, offset));
                        continue;
                    }

                    hasOptionalDecimalPart = true;
                }

                // number: [+-]?d+(.|.d+)?([eE][+-]?d+)?
                // optional decimal part: ([eE][+-]?d+)?
                cc = reader.charAt(i);
                if ('e' == cc || 'E' == cc) {
                    if (i + 1 < limit) {
                        // is it the optional sign or a digit?
                        cc = reader.charAt(i + 1);
                        if (PLUS_CHAR == cc || MINUS_CHAR == cc || Character.isDigit(cc)) {
                            if (PLUS_CHAR == cc || MINUS_CHAR == cc) {
                                if (i + 2 < limit &&
                                        Character.isDigit(reader.charAt(i + 2))) {
                                    i += 2;
                                } else {
                                    String number = reader.substring(numberStart, i - numberStart);
                                    SymbolType type = hasOptionalDecimalPart ? SymbolType.FLOATING : SymbolType.INTEGER;
                                    int line = reader.lineNumberFor(i);
                                    int offset = reader.charOffsetInLine(i);
                                    symbols.add(Symbol.get(number, type, line, offset));
                                    i--;
                                    continue;
                                }
                            } else {
                                i += 1;
                            }

                            // skip digits
                            while (i < limit &&
                                    Character.isDigit(reader.charAt(i))) {
                                i++;
                            }

                            if (i >= limit) {
                                String number = reader.substring(numberStart, i - numberStart);
                                int line = reader.lineNumberFor(numberStart);
                                int offset = reader.charOffsetInLine(numberStart);
                                symbols.add(Symbol.get(number, Symbol.SymbolType.FLOATING, line, offset));
                                continue;
                            }

                            hasOptionalScientificNotationPart = true;
                        }
                    }
                }

                String number = reader.substring(numberStart, i - numberStart);
                SymbolType type = hasOptionalDecimalPart || hasOptionalScientificNotationPart ?
                        SymbolType.FLOATING : SymbolType.INTEGER;
                int line = reader.lineNumberFor(i);
                int offset = reader.charOffsetInLine(i);
                symbols.add(Symbol.get(number, type, line, offset));
                i--;
                continue;
            }

            // parentheses
            if (OPEN_P_CHAR == c) {
                int line = reader.lineNumberFor(i);
                int offset = reader.charOffsetInLine(i);
                symbols.add(Symbol.get("(", Symbol.SymbolType.OPEN_P, line, offset));
            } else if (CLOSE_P_CHAR == c) {
                int line = reader.lineNumberFor(i);
                int offset = reader.charOffsetInLine(i);
                symbols.add(Symbol.get(")", Symbol.SymbolType.CLOSE_P, line, offset));
            }
            // brackets
            else if (OPEN_B_CHAR == c) {
                int line = reader.lineNumberFor(i);
                int offset = reader.charOffsetInLine(i);
                symbols.add(Symbol.get("[", Symbol.SymbolType.OPEN_B, line, offset));
            } else if (CLOSE_B_CHAR == c) {
                int line = reader.lineNumberFor(i);
                int offset = reader.charOffsetInLine(i);
                symbols.add(Symbol.get("]", Symbol.SymbolType.CLOSE_B, line, offset));
            }
            // operators
            else if (FIRST_OF_OPERATORS.contains(c)) {
                if (GT_CHAR == c || LT_CHAR == c || NOT_CHAR == c) {
                    if (i + 1 < limit) {
                        if (EQ_CHAR == reader.charAt(i + 1)) {
                            int line = reader.lineNumberFor(i);
                            int offset = reader.charOffsetInLine(i);
                            symbols.add(
                                    Symbol.get(reader.substring(i, 2), Symbol.SymbolType.COMPARISON_OP, line, offset));
                            i++;
                            continue;
                        }
                    }
                }
                String op = String.valueOf(c);
                SymbolType type = resolveSymbolType(c);
                int line = reader.lineNumberFor(i);
                int offset = reader.charOffsetInLine(i);
                symbols.add(Symbol.get(op, type, line, offset));
            }
            // references and keywords
            else if (isValidRefOrKeywordStart(c)) {
                int termStart = i;
                i++; // skip first char of reference or keyword

                // skip to the end while we find chars that belong to a term or keyword
                while (i < limit && isValidRefOrKeywordChar(reader.charAt(i))) {
                    i++;
                }
                String term = reader.substring(termStart, i - termStart);
                Symbol.SymbolType type = KEYWORDS.contains(term) ? Symbol.SymbolType.KEYWORD : Symbol.SymbolType.REFERENCE;
                int line = reader.lineNumberFor(termStart);
                int offset = reader.charOffsetInLine(termStart);
                symbols.add(Symbol.get(term, type, line, offset));
                i--;
            } else {
                System.err.printf("Ignoring unknown character %c found at offset %d\n", c, i);
            }
        }
        return this;
    }

    private static SymbolType resolveSymbolType(char c) {
        SymbolType type = SymbolType.UNKNOWN;
        if (MINUS_CHAR == c || PLUS_CHAR == c || PROD_CHAR == c || DIV_CHAR == c || MOD_CHAR == c) {
            type = SymbolType.ARITHMETIC_OP;
        } else if (EQ_CHAR == c || LT_CHAR == c || GT_CHAR == c) {
            type = SymbolType.COMPARISON_OP;
        } else if (OR_CHAR == c || AND_CHAR == c || NOT_CHAR == c) {
            type = SymbolType.LOGIC_OP;
        } else if (DOT_CHAR == c) {
            type = SymbolType.DOT;
        }
        assert SymbolType.UNKNOWN != type;
        return type;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public boolean isFromFile() {
        return null != reader.getFile();
    }

    public String getFileName() {
        File file = reader.getFile();
        return null != file ? file.getAbsolutePath() : NOT_FROM_FILE;
    }

    @Override
    public Iterator<Symbol> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return currentSymbolOffset < symbols.size();
    }

    @Override
    public Symbol next() {
        return hasNext() ? symbols.get(currentSymbolOffset++) : null;
    }

    public int getCurrentSymbolOffset() {
        return currentSymbolOffset;
    }

    @Override
    public void remove() {
        throw new RuntimeException("method left intentionally unimplemented");
    }

    @Override
    public String toString() {
        return reader.toString();
    }
}