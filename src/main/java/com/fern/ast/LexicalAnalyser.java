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
  private static final Set<String> FIRST_OF_OPERATORS = new HashSet<>(
      Arrays.asList(new String[]{ "-", "+", "*", "/", "%", "&", "|", "<", ">", "=", "!", "." }));

  private static final Set<String> KEYWORDS;
  static {
    final Set<String> keywords = new HashSet<>();
    for (Symbol.Keyword k : Symbol.Keyword.values()) {
      keywords.add(k.key);
    }
    KEYWORDS = Collections.unmodifiableSet(keywords);
  }

  private static boolean isValidRefOrKeywordStart(final char c) {
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
  public final static LexicalAnalyser get(SourceContents reader) {
    return new LexicalAnalyser(reader);
  }

  /**
   * Factory method that uses an internal LexicalAnalyser on the text
   * to get a list of Symbols
   * 
   * @param text
   * @return The list of Symbols from the text
   */
  public final static List<Symbol> getSymbols(String text) {
    return new LexicalAnalyser(SourceContents.fromText(text)).analyse().getSymbols();
  }

  public final static void spit(String text) {
    for (Symbol sym : getSymbols(text)) {
      System.out.println(sym);
    }
  }

  private final SourceContents reader;
  private final List<Symbol> symbols;
  private int currentSymbolOffset;

  private LexicalAnalyser(SourceContents reader) {
    this.reader = reader;
    this.symbols = new ArrayList<>();
  }

  /**
   * Performs the lexical analysis from scratch
   */
  public final LexicalAnalyser analyse() {
    this.symbols.clear();
    this.currentSymbolOffset = 0;
    for (int currentCharOffset = 0; currentCharOffset < this.reader.length(); currentCharOffset++) {
      final char c = this.reader.charAt(currentCharOffset);

      // whites
      if (Character.isWhitespace(c)) {
        continue;
      }

      // comments
      if (COMMENT_CHAR == c) {
        currentCharOffset++; // consume the comment start char
        final int commentStart = currentCharOffset;
        char cc = c;
        while (currentCharOffset < this.reader.length()) {
          cc = this.reader.charAt(currentCharOffset);
          currentCharOffset++;
          if (COMMENT_CHAR == cc || NEWLINE_CHAR == cc) {
            break;
          }
        }
        int commentLength = currentCharOffset - commentStart;
        if ((COMMENT_CHAR == cc || NEWLINE_CHAR == cc) && currentCharOffset > commentStart) {
          commentLength--;
        }
        final String comment = this.reader.substring(commentStart, commentLength);
        final int line = this.reader.lineNumberFor(commentStart);
        final int offset = this.reader.charOffsetInLine(commentStart);
        this.symbols.add(Symbol.get(comment, Symbol.SymbolType.COMMENT, line, offset));
        currentCharOffset--;
        continue;
      }

      // string
      if (STRING_CHAR == c) {
        currentCharOffset++; // consume string start char
        final int stringStart = currentCharOffset;
        while (currentCharOffset < this.reader.length()) {
          char cc = this.reader.charAt(currentCharOffset);
          currentCharOffset++;
          if (STRING_CHAR == cc) {
            break;
          }
        }
        if (currentCharOffset >= this.reader.length() && STRING_CHAR != this.reader.charAt(currentCharOffset - 1)) {
          throw new RuntimeException(String.format("String starting with %c at offset %d has not been closed",
              STRING_CHAR, Integer.valueOf(stringStart)));
        }
        currentCharOffset--;
        final String string = this.reader.substring(stringStart, currentCharOffset - stringStart);
        final int line = this.reader.lineNumberFor(stringStart);
        final int offset = this.reader.charOffsetInLine(stringStart);
        this.symbols.add(Symbol.get(string, Symbol.SymbolType.STRING, line, offset));
        continue;
      }

      // number: [+-]?d+(.|.d+)?([eE][+-]?d+)?
      if (PLUS_CHAR == c || MINUS_CHAR == c || Character.isDigit(c)) {
        final int numberStart = currentCharOffset;
        boolean hasOptionalDecimalPart = false;
        boolean hasOptionalScientificNotationPart = false;

        // check whether the sign is really a sign or an operator
        if (PLUS_CHAR == c || MINUS_CHAR == c) {
          if (currentCharOffset + 1 < this.reader.length()) {
            if (false == Character.isDigit(this.reader.charAt(currentCharOffset + 1))) {
              // signs must be immediately next to a digit to be considered a sign
              final int line = this.reader.lineNumberFor(currentCharOffset);
              final int offset = this.reader.charOffsetInLine(currentCharOffset);
              this.symbols.add(Symbol.get(String.valueOf(c), Symbol.SymbolType.ARITHMETIC_OP, line, offset));
              continue;
            }
          }
          else {
            // We found the end of the file at a sign, so it becomes an operator
            final int line = this.reader.lineNumberFor(currentCharOffset);
            final int offset = this.reader.charOffsetInLine(currentCharOffset);
            this.symbols.add(Symbol.get(String.valueOf(c), Symbol.SymbolType.ARITHMETIC_OP, line, offset));
            continue;
          }
        }

        currentCharOffset++; // skip either sign or first digit

        // skip all digits
        while (currentCharOffset < this.reader.length() && Character.isDigit(this.reader.charAt(currentCharOffset))) {
          currentCharOffset++;
        }

        // if we found the end of the file we are done
        if (currentCharOffset >= this.reader.length()) {
          final String number = this.reader.substring(numberStart, currentCharOffset - numberStart);
          final int line = this.reader.lineNumberFor(numberStart);
          final int offset = this.reader.charOffsetInLine(numberStart);
          this.symbols.add(Symbol.get(number, Symbol.SymbolType.INTEGER, line, offset));
          continue;
        }

        // number: [+-]?d+(.|.d+)?([eE][+-]?d+)?
        // optional decimal part: (.|.d+)?
        char cc = this.reader.charAt(currentCharOffset);
        if (DOT_CHAR == cc) {
          currentCharOffset++; // skip dot

          if (currentCharOffset >= this.reader.length()) {
            final String number = this.reader.substring(numberStart, currentCharOffset - numberStart);
            final int line = this.reader.lineNumberFor(numberStart);
            final int offset = this.reader.charOffsetInLine(numberStart);
            this.symbols.add(Symbol.get(number, Symbol.SymbolType.FLOATING, line, offset));
            continue;
          }

          // skip digits
          while (currentCharOffset < this.reader.length() && Character.isDigit(this.reader.charAt(currentCharOffset))) {
            currentCharOffset++;
          }

          if (currentCharOffset >= this.reader.length()) {
            final String number = this.reader.substring(numberStart, currentCharOffset - numberStart);
            final int line = this.reader.lineNumberFor(numberStart);
            final int offset = this.reader.charOffsetInLine(numberStart);
            this.symbols.add(Symbol.get(number, Symbol.SymbolType.FLOATING, line, offset));
            continue;
          }

          hasOptionalDecimalPart = true;
        }

        // number: [+-]?d+(.|.d+)?([eE][+-]?d+)?
        // optional decimal part: ([eE][+-]?d+)?
        cc = this.reader.charAt(currentCharOffset);
        if ('e' == cc || 'E' == cc) {
          if (currentCharOffset + 1 < this.reader.length()) {
            // is it the optional sign or a digit?
            cc = this.reader.charAt(currentCharOffset + 1);
            if (PLUS_CHAR == cc || MINUS_CHAR == cc || Character.isDigit(cc)) {
              if (PLUS_CHAR == cc || MINUS_CHAR == cc) {
                if (currentCharOffset + 2 < this.reader.length() && 
                    Character.isDigit(this.reader.charAt(currentCharOffset + 2))) {
                  currentCharOffset += 2;
                }
                else {
                  final String number = this.reader.substring(numberStart, currentCharOffset - numberStart);
                  final SymbolType type = hasOptionalDecimalPart ? SymbolType.FLOATING : SymbolType.INTEGER;
                  final int line = this.reader.lineNumberFor(currentCharOffset);
                  final int offset = this.reader.charOffsetInLine(currentCharOffset);
                  this.symbols.add(Symbol.get(number, type, line, offset));
                  currentCharOffset--;
                  continue;
                }
              }
              else {
                currentCharOffset += 1;
              }

              // skip digits
              while (currentCharOffset < this.reader.length() && 
                  Character.isDigit(this.reader.charAt(currentCharOffset))) {
                currentCharOffset++;
              }

              if (currentCharOffset >= this.reader.length()) {
                final String number = this.reader.substring(numberStart, currentCharOffset - numberStart);
                final int line = this.reader.lineNumberFor(numberStart);
                final int offset = this.reader.charOffsetInLine(numberStart);
                this.symbols.add(Symbol.get(number, Symbol.SymbolType.FLOATING, line, offset));
                continue;
              }

              hasOptionalScientificNotationPart = true;
            }
          }
        }

        final String number = this.reader.substring(numberStart, currentCharOffset - numberStart);
        final SymbolType type = hasOptionalDecimalPart || hasOptionalScientificNotationPart ? 
            SymbolType.FLOATING : SymbolType.INTEGER;
        final int line = this.reader.lineNumberFor(currentCharOffset);
        final int offset = this.reader.charOffsetInLine(currentCharOffset);
        this.symbols.add(Symbol.get(number, type, line, offset));
        currentCharOffset--;
        continue;
      }

      // parentheses
      if (OPEN_P_CHAR == c) {
        final int line = this.reader.lineNumberFor(currentCharOffset);
        final int offset = this.reader.charOffsetInLine(currentCharOffset);
        this.symbols.add(Symbol.get("(", Symbol.SymbolType.OPEN_P, line, offset));
        continue;
      }
      else if (CLOSE_P_CHAR == c) {
        final int line = this.reader.lineNumberFor(currentCharOffset);
        final int offset = this.reader.charOffsetInLine(currentCharOffset);
        this.symbols.add(Symbol.get(")", Symbol.SymbolType.CLOSE_P, line, offset));
        continue;
      }
      // brackets
      if (OPEN_B_CHAR == c) {
        final int line = this.reader.lineNumberFor(currentCharOffset);
        final int offset = this.reader.charOffsetInLine(currentCharOffset);
        this.symbols.add(Symbol.get("[", Symbol.SymbolType.OPEN_B, line, offset));
        continue;
      }
      else if (CLOSE_B_CHAR == c) {
        final int line = this.reader.lineNumberFor(currentCharOffset);
        final int offset = this.reader.charOffsetInLine(currentCharOffset);
        this.symbols.add(Symbol.get("]", Symbol.SymbolType.CLOSE_B, line, offset));
        continue;
      }
      // operators
      else if (FIRST_OF_OPERATORS.contains(String.valueOf(c))) {
        if (GT_CHAR == c || LT_CHAR == c || NOT_CHAR == c) {
          if (currentCharOffset + 1 < this.reader.length()) {
            if (EQ_CHAR == this.reader.charAt(currentCharOffset + 1)) {
              final int line = this.reader.lineNumberFor(currentCharOffset);
              final int offset = this.reader.charOffsetInLine(currentCharOffset);
              this.symbols.add(
                  Symbol.get(this.reader.substring(currentCharOffset, 2), Symbol.SymbolType.COMPARISON_OP, line, offset));
              currentCharOffset++;
              continue;
            }
          }
        }
        final String op = String.valueOf(c);
        SymbolType type = SymbolType.UNKNOWN;
        if (MINUS_CHAR == c || PLUS_CHAR == c || PROD_CHAR == c || DIV_CHAR == c || MOD_CHAR == c) {
          type = SymbolType.ARITHMETIC_OP;
        }
        else if (EQ_CHAR == c || LT_CHAR == c || GT_CHAR == c) {
          type = SymbolType.COMPARISON_OP;
        }
        else if (OR_CHAR == c || AND_CHAR == c || NOT_CHAR == c) {
          type = SymbolType.LOGIC_OP;
        }
        else if (DOT_CHAR == c) {
          type = SymbolType.DOT;
        }
        assert SymbolType.UNKNOWN != type;
        final int line = this.reader.lineNumberFor(currentCharOffset);
        final int offset = this.reader.charOffsetInLine(currentCharOffset);
        this.symbols.add(Symbol.get(op, type, line, offset));
        continue;
      }
      // references and keywords
      else if (isValidRefOrKeywordStart(c)) {
        final int termStart = currentCharOffset;
        currentCharOffset++; // skip first char of reference or keyword

        // skip to the end while we find chars that belong to a term or keyword
        while (currentCharOffset < this.reader.length() && isValidRefOrKeywordChar(this.reader.charAt(currentCharOffset))) {
          currentCharOffset++;
        }
        final String term = this.reader.substring(termStart, currentCharOffset - termStart);
        final Symbol.SymbolType type = KEYWORDS.contains(term)? Symbol.SymbolType.KEYWORD : Symbol.SymbolType.REFERENCE;
        final int line = this.reader.lineNumberFor(termStart);
        final int offset = this.reader.charOffsetInLine(termStart);
        this.symbols.add(Symbol.get(term, type, line, offset));
        currentCharOffset--;
        continue;
      }
      else {
        System.err.printf("Ignoring unknown character %c found at offset %d\n", c, currentCharOffset);
      }
    }

    return this;
  }

  public final List<Symbol> getSymbols() {
    return this.symbols;
  }

  public final boolean isFromFile() {
    return null != this.reader.getFile();
  }

  public final String getFileName() {
    final File file = this.reader.getFile();
    return null != file ? file.getAbsolutePath() : NOT_FROM_FILE;
  }

  @Override
  public final Iterator<Symbol> iterator() {
    return this;
  }

  @Override
  public final boolean hasNext() {
    return this.currentSymbolOffset < this.symbols.size();
  }

  @Override
  public final Symbol next() {
    return hasNext() ? this.symbols.get(this.currentSymbolOffset++) : null;
  }

  public final int getCurrentSymbolOffset() {
    return this.currentSymbolOffset;
  }

  @Override
  public final void remove() {
    throw new RuntimeException("Method left intentionally unimplemented");
  }

  @Override
  public final String toString() {
    return this.reader.toString();
  }
}