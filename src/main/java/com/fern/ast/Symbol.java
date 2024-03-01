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
            return key;
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
        COMMENT
    }

    public static Symbol get(final String value, final SymbolType type) {
        return new Symbol(value, type, -1, -1);
    }

    public static Symbol get(final String value, final SymbolType type, final int line, final int offset) {
        return new Symbol(value, type, line, offset);
    }

    private final SymbolType type;
    private final int line;
    private final int offset;
    private final String stringValue;
    private final AtomicReference<Double> floatingValue;
    private final AtomicReference<Long> integerValue;

    private Symbol(String stringValue, SymbolType type, int line, int offset) {
        this.stringValue = stringValue;
        this.type = type;
        this.line = line;
        this.offset = offset;
        this.floatingValue = new AtomicReference<>();
        this.integerValue = new AtomicReference<>();
    }

    public final String getStringValue() {
        return stringValue;
    }

    public final SymbolType getType() {
        return type;
    }

    /**
     * @return Symbol's line number (-1 when token is not read from a file)
     */
    public final int getLine() {
        return line;
    }

    /**
     * @return Symbol's offset (-1 when token is not read from a file)
     */
    public final int getOffset() {
        return offset;
    }

    public final boolean isOpenParens() {
        return SymbolType.OPEN_P == type;
    }

    public final boolean isCloseParens() {
        return SymbolType.CLOSE_P == type;
    }

    public final boolean isString() {
        return SymbolType.STRING == type;
    }

    public final boolean isInteger() {
        return SymbolType.INTEGER == type;
    }

    public final boolean isFloating() {
        return SymbolType.FLOATING == type;
    }

    /**
     * @return tests whether the Symbol is a floating point, integer number or a string
     */
    public final boolean isLiteral() {
        return SymbolType.FLOATING == type || SymbolType.INTEGER == type || SymbolType.STRING == type;
    }

    public final boolean isArithmeticOperator() {
        return SymbolType.ARITHMETIC_OP == type;
    }

    public final boolean isLogicOperator() {
        return SymbolType.LOGIC_OP == type;
    }

    public final boolean isComparisonOperator() {
        return SymbolType.COMPARISON_OP == type;
    }

    /**
     * @return tests whether the Symbol is a reference (a list of chars not enclosed within quotes)
     */
    public final boolean isReference() {
        return SymbolType.REFERENCE == type;
    }

    /**
     * @return tests whether the Symbol is a keyword (a list of chars not enclosed within quotes
     * that is a reserved word in the language)
     */
    public final boolean isKeyword() {
        return SymbolType.KEYWORD == type;
    }

    public final boolean isComment() {
        return SymbolType.COMMENT == type;
    }

    public final String getValue() {
        return stringValue;
    }

    /**
     * Attempts to cast the token's text to a double and return it.
     * It will return 0.00 if the operation is not possible
     *
     * @return The Symbol's numeric value
     */
    public final Double getFloatingValue() {
        Double dbl = floatingValue.get();
        if (null == dbl) {
            floatingValue.compareAndSet(null, dbl = isFloating() ? Double.parseDouble(stringValue) : 0.0d);
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
            integerValue.compareAndSet(null, n = isInteger() ? Long.parseLong(stringValue) : 0);
        }
        return n;
    }

    @Override
    public final String toString() {
        return String.format("[line:%d, char:%d, Type:%s, Text:%s]", line, offset, type, stringValue);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Symbol) {
            final Symbol that = (Symbol) o;
            return stringValue.equals(stringValue) && type == type;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return stringValue.hashCode() * 11 + type.hashCode() * 17;
    }
}