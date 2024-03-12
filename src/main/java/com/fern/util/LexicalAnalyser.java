package com.fern.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fern.util.Symbol.Type;

public final class LexicalAnalyser implements Iterable<Symbol>, Iterator<Symbol> {
    private static final String NOT_FROM_FILE = "NOT FROM FILE";

    public static final char OPEN_P = '(';
    public static final char CLOSE_P = ')';
    public static final char OPEN_B = '[';
    public static final char CLOSE_B = ']';

    private static final char NEWLINE = '\n';
    private static final char COMMENT = ';';
    private static final char SPACE = ' ';
    private static final char STRING = '"';

    private static final char MINUS = '-';
    private static final char PLUS = '+';
    private static final char PROD = '*';
    private static final char DIV = '/';
    private static final char MOD = '%';
    private static final char DOT = '.';
    private static final char AND = '&';
    private static final char OR = '|';
    private static final char GT = '>';
    private static final char LT = '<';
    private static final char EQ = '=';
    private static final char NOT = '!';
    private static final Set<Character> FIRST_OF_OPERATORS = new HashSet<>(Arrays.asList(
            MINUS,
            PLUS,
            PROD,
            DIV,
            MOD,
            AND,
            OR,
            LT,
            GT,
            EQ,
            NOT,
            DOT
    ));

    private static final Set<String> KEYWORDS = Arrays
            .stream(Symbol.Keyword.values())
            .map(Symbol.Keyword::toString)
            .collect(Collectors.toUnmodifiableSet());

    private static boolean isValidRefOrKeywordStart(char c) {
        return c >= ('a' | 32) && c <= ('z' | 32) || c == '_';
    }

    private static boolean isValidRefOrKeywordChar(char c) {
        return isValidRefOrKeywordStart(c) || (c >= '0' && c <= '9');
    }

    public static LexicalAnalyser get(Source reader) {
        return new LexicalAnalyser(reader);
    }

    public static List<Symbol> extractSymbols(String text) {
        return new LexicalAnalyser(TextSource.create(text)).analyse().extractSymbols();
    }

    private final Source reader;
    private final List<Symbol> symbols;
    private int currentSymbolOffset;

    private LexicalAnalyser(Source reader) {
        this.reader = reader;
        this.symbols = new ArrayList<>();
    }

    private void addSymbol(int start, int end, Type type) {
        int len = end - start;
        symbols.add(Symbol.create(
                len == 0 ? "" : reader.substring(start, len),
                type,
                reader.lineNumber(start),
                reader.charOffsetInLine(start)));
    }

    /**
     * Performs the lexical analysis from scratch
     */
    public LexicalAnalyser analyse() {
        symbols.clear();
        currentSymbolOffset = 0;
        int limit = reader.length();
        for (int i = 0; i < limit; i++) {
            char c = reader.charAt(i);

            // whites
            if (Character.isWhitespace(c)) {
                continue;
            }

            // comments
            if (COMMENT == c) {
                while (++i < limit && ((c = reader.charAt(i)) == COMMENT || c == SPACE)) {
                    // skip comment prefix
                }
                int start = i;
                while (i < limit && (c = reader.charAt(i)) != COMMENT && c != NEWLINE) {
                    i++;
                }
                addSymbol(start, i, Type.COMMENT);
                continue;
            }

            // string
            if (STRING == c) {
                i++; // consume string start char
                int start = i;
                while (i < limit && (c = reader.charAt(i)) != STRING) {
                    i++;
                }
                if (i >= limit && c != STRING) {
                    throw new RuntimeException(Util.str(
                            "string starting with %c at offset %d has not been closed",
                            STRING, start
                    ));
                }
                addSymbol(start, i, Type.STRING);
                continue;
            }

            // number: [+-]?d+(.|.d+)?([eE][+-]?d+)?
            if (PLUS == c || MINUS == c || Character.isDigit(c)) {
                // check whether the sign is really a sign or an operator
                if (PLUS == c || MINUS == c) {
                    if (i + 1 >= limit || !Character.isDigit(reader.charAt(i + 1))) {
                        addSymbol(i, i + 1, Type.ARITHMETIC_OP);
                        continue;
                    }
                }
                int start = i;
                i++; // skip either sign or first digit
                // skip all digits
                while (i < limit && Character.isDigit(reader.charAt(i))) {
                    i++;
                }
                // if we found the end of the file we are done
                if (i >= limit) {
                    addSymbol(start, i, Type.INTEGER);
                    continue;
                }

                // number: [+-]?d+(.|.d+)?([eE][+-]?d+)?
                // optional decimal part: (.|.d+)?
                boolean hasOptionalDecimalPart = false;
                boolean hasOptionalScientificNotationPart = false;
                char cc = reader.charAt(i);
                if (DOT == cc) {
                    i++; // skip dot
                    if (i >= limit) {
                        addSymbol(start, i, Type.FLOATING);
                        continue;
                    }
                    // skip digits
                    while (i < limit && Character.isDigit(reader.charAt(i))) {
                        i++;
                    }
                    if (i >= limit) {
                        addSymbol(start, i, Type.FLOATING);
                        continue;
                    }
                    hasOptionalDecimalPart = true;
                }

                // number: [+-]?d+(.|.d+)?([eE][+-]?d+)?
                // optional decimal part: ([eE][+-]?d+)?
                cc = reader.charAt(i);
                if ('e' == (cc | 32)) {
                    if (i + 1 < limit) {
                        // is it the optional sign or a digit?
                        cc = reader.charAt(i + 1);
                        if (PLUS == cc || MINUS == cc || Character.isDigit(cc)) {
                            if (PLUS == cc || MINUS == cc) {
                                if (i + 2 < limit && Character.isDigit(reader.charAt(i + 2))) {
                                    i += 2;
                                } else {
                                    addSymbol(start, i, hasOptionalDecimalPart ? Type.FLOATING : Type.INTEGER);
                                    i--;
                                    continue;
                                }
                            } else {
                                i += 1;
                            }
                            // skip digits
                            while (i < limit && Character.isDigit(reader.charAt(i))) {
                                i++;
                            }
                            if (i >= limit) {
                                addSymbol(start, i, Type.FLOATING);
                                continue;
                            }
                            hasOptionalScientificNotationPart = true;
                        }
                    }
                }
                addSymbol(start, i,
                        hasOptionalDecimalPart || hasOptionalScientificNotationPart ?
                                Type.FLOATING : Type.INTEGER);
                i--;
                continue;
            }

            // parentheses
            if (OPEN_P == c) {
                addSymbol(i, i + 1, Type.OPEN_P);
            } else if (CLOSE_P == c) {
                addSymbol(i, i + 1, Type.CLOSE_P);
            }
            // brackets
            else if (OPEN_B == c) {
                addSymbol(i, i + 1, Type.OPEN_B);
            } else if (CLOSE_B == c) {
                addSymbol(i, i + 1, Type.CLOSE_B);
            }
            // operators
            else if (FIRST_OF_OPERATORS.contains(c)) {
                if (GT == c || LT == c || NOT == c) {
                    if (i + 1 < limit) {
                        if (EQ == reader.charAt(i + 1)) {
                            addSymbol(i, i + 2, Type.COMPARISON_OP);
                            i++;
                            continue;
                        }
                    }
                }
                addSymbol(i, i + 1, resolveSymbolType(c));
            }
            // references and keywords
            else if (isValidRefOrKeywordStart(c)) {
                int start = i;
                i++; // skip first char of reference or keyword
                // skip to the end while we find chars that belong to a term or keyword
                while (i < limit && isValidRefOrKeywordChar(reader.charAt(i))) {
                    i++;
                }
                String term = reader.substring(start, i - start);
                Type type = KEYWORDS.contains(term) ? Type.KEYWORD : Type.REFERENCE;
                int line = reader.lineNumber(start);
                int offset = reader.charOffsetInLine(start);
                symbols.add(Symbol.create(term, type, line, offset));
                i--;
            } else {
                System.err.printf("Ignoring unknown character %c found at offset %d\n", c, i);
            }
        }
        return this;
    }

    private static Type resolveSymbolType(char c) {
        Type type = Type.UNKNOWN;
        if (MINUS == c || PLUS == c || PROD == c || DIV == c || MOD == c) {
            type = Type.ARITHMETIC_OP;
        } else if (EQ == c || LT == c || GT == c) {
            type = Type.COMPARISON_OP;
        } else if (OR == c || AND == c || NOT == c) {
            type = Type.LOGIC_OP;
        } else if (DOT == c) {
            type = Type.DOT;
        }
        assert Type.UNKNOWN != type;
        return type;
    }

    public List<Symbol> extractSymbols() {
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