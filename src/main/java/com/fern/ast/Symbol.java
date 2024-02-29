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

import java.util.concurrent.atomic.AtomicReference;

public final class Symbol {
  public enum Keyword {
    DEF("def"), NEW("new");

    public final String key;
    Keyword(String key) {
      this.key = key;
    }

    @Override
    public final String toString() {
      return this.key;
    }
  }

  public enum SymbolType {
    UNKNOWN, 
    OPEN_P, CLOSE_P, 
    OPEN_B, CLOSE_B, 
    REFERENCE, KEYWORD, 
    STRING, INTEGER, FLOATING, 
    ARITHMETIC_OP, COMPARISON_OP, LOGIC_OP, 
    DOT, 
    COMMENT;
  }

  public static final Symbol get(final String value, final SymbolType type) {
    return new Symbol(value, type, -1, -1);
  }

  public static final Symbol get(final String value, final SymbolType type, final int line, final int offset) {
    return new Symbol(value, type, line, offset);
  }

  private final SymbolType type;
  private final int line;
  private final int offset;
  private final String stringValue;
  private final AtomicReference<Double> floatingValue;
  private final AtomicReference<Long> integerValue;

  private Symbol(final String stringValue, final SymbolType type, final int line, final int offset) {
    this.stringValue = stringValue;
    this.type = type;
    this.line = line;
    this.offset = offset;
    this.floatingValue = new AtomicReference<>();
    this.integerValue = new AtomicReference<>();
  }

  public final String getStringValue() {
    return this.stringValue;
  }

  public final SymbolType getType() {
    return this.type;
  }

  /**
   * @return Symbol's line number (-1 when token is not read from a file)
   */
  public final int getLine() {
    return this.line;
  }

  /**
   * @return Symbol's offset (-1 when token is not read from a file)
   */
  public final int getOffset() {
    return this.offset;
  }

  public final boolean isOpenParens() {
    return SymbolType.OPEN_P == this.type;
  }

  public final boolean isCloseParens() {
    return SymbolType.CLOSE_P == this.type;
  }

  public final boolean isString() {
    return SymbolType.STRING == this.type;
  }

  public final boolean isInteger() {
    return SymbolType.INTEGER == this.type;
  }

  public final boolean isFloating() {
    return SymbolType.FLOATING == this.type;
  }

  /**
   * @return tests whether the Symbol is a floating point, integer number or a string
   */
  public final boolean isLiteral() {
    return SymbolType.FLOATING == this.type || SymbolType.INTEGER == this.type || SymbolType.STRING == this.type;
  }

  public final boolean isArithmeticOperator() {
    return SymbolType.ARITHMETIC_OP == this.type;
  }

  public final boolean isLogicOperator() {
    return SymbolType.LOGIC_OP == this.type;
  }

  public final boolean isComparisonOperator() {
    return SymbolType.COMPARISON_OP == this.type;
  }

  /**
   * @return tests whether the Symbol is a reference (a list of chars not enclosed within quotes)
   */
  public final boolean isReference() {
    return SymbolType.REFERENCE == this.type;
  }

  /**
   * @return tests whether the Symbol is a keyword (a list of chars not enclosed within quotes
   *         that is a reserved word in the language)
   */
  public final boolean isKeyword() {
    return SymbolType.KEYWORD == this.type;
  }

  public final boolean isComment() {
    return SymbolType.COMMENT == this.type;
  }

  public final String getValue() {
    return this.stringValue;
  }

  /**
   * Attempts to cast the token's text to a double and return it.
   * It will return 0.00 if the operation is not possible
   * 
   * @return The Symbol's numeric value
   */
  public final Double getFloatingValue() {
    Double dbl = this.floatingValue.get();
    if (null == dbl) {
      synchronized (this.floatingValue) {
        dbl = this.floatingValue.get();
        if (null == dbl) {
          this.floatingValue.set(dbl = Double.valueOf(isFloating() ? this.stringValue : "0.000"));
        }
      }
    }
    return dbl;
  }

  /**
   * Attempts to cast the token's text to a long and return it.
   * It will return 0 if the operation is not possible
   * 
   * @return The Symbol's numeric value
   */
  public final Long getIntegerValue() {
    Long n = this.integerValue.get();
    if (null == n) {
      synchronized (this.integerValue) {
        n = this.integerValue.get();
        if (null == n) {
          this.integerValue.set(n = Long.valueOf(isInteger() ? this.stringValue : "0"));
        }
      }
    }
    return n;
  }

  @Override
  public final String toString() {
    return String.format("[line:%d, char:%d, Type:%s, Text:%s]", this.line, this.offset, this.type, this.stringValue);
  }

  @Override
  public final boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (null != o && o instanceof Symbol) {
      final Symbol that = (Symbol) o;
      return this.stringValue.equals(that.stringValue) && this.type == that.type;
    }
    return false;
  }

  @Override
  public final int hashCode() {
    return this.stringValue.hashCode() * 11 + this.type.hashCode() * 17;
  }
}