package com.fern.util;

import java.util.concurrent.atomic.AtomicReference;

public final class Symbol {
    public enum Keyword {
        DEF("def"),
        NEW("new");

        public final String key;

        Keyword(String key) {
            this.key = key;
        }

        @Override
        public final String toString() {
            return key;
        }
    }

    public enum Type {
        UNKNOWN,
        OPEN_P, CLOSE_P,
        OPEN_B, CLOSE_B,
        REFERENCE, KEYWORD,
        STRING, INTEGER, FLOATING,
        ARITHMETIC_OP, COMPARISON_OP, LOGIC_OP,
        DOT,
        COMMENT
    }

    public static Symbol create(final String value, final Type type) {
        return new Symbol(value, type, -1, -1);
    }

    public static Symbol create(final String value, final Type type, final int line, final int offset) {
        return new Symbol(value, type, line, offset);
    }

    private final Type type;
    private final int line;
    private final int offset;
    private final String text;
    private final AtomicReference<Double> floatingValue;
    private final AtomicReference<Long> integerValue;

    private Symbol(String stringValue, Type type, int line, int offset) {
        this.text = stringValue;
        this.type = type;
        this.line = line;
        this.offset = offset;
        this.floatingValue = new AtomicReference<>();
        this.integerValue = new AtomicReference<>();
    }

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }

    /**
     * @return Symbol's line number (-1 when token is not read from a file)
     */
    public int getLine() {
        return line;
    }

    /**
     * @return Symbol's offset (-1 when token is not read from a file)
     */
    public int getOffset() {
        return offset;
    }

    public boolean isOpenParens() {
        return Type.OPEN_P == type;
    }

    public boolean isCloseParens() {
        return Type.CLOSE_P == type;
    }

    public boolean isString() {
        return Type.STRING == type;
    }

    public boolean isInteger() {
        return Type.INTEGER == type;
    }

    public boolean isFloating() {
        return Type.FLOATING == type;
    }

    /**
     * @return tests whether the Symbol is a floating point, integer number or a string
     */
    public boolean isLiteral() {
        return Type.FLOATING == type || Type.INTEGER == type || Type.STRING == type;
    }

    public boolean isArithmeticOperator() {
        return Type.ARITHMETIC_OP == type;
    }

    public boolean isLogicOperator() {
        return Type.LOGIC_OP == type;
    }

    public boolean isComparisonOperator() {
        return Type.COMPARISON_OP == type;
    }

    /**
     * @return tests whether the Symbol is a reference (a list of chars not enclosed within quotes)
     */
    public boolean isReference() {
        return Type.REFERENCE == type;
    }

    /**
     * @return tests whether the Symbol is a keyword (a list of chars not enclosed within quotes
     * that is a reserved word in the language)
     */
    public boolean isKeyword() {
        return Type.KEYWORD == type;
    }

    public boolean isComment() {
        return Type.COMMENT == type;
    }

    public String getValue() {
        return text;
    }

    /**
     * Attempts to cast the token's text to a double and return it.
     * It will return 0.00 if the operation is not possible
     *
     * @return The Symbol's numeric value
     */
    public Double getFloatingValue() {
        Double dbl = floatingValue.get();
        if (dbl != null) {
            return dbl;
        }
        return floatingValue.compareAndSet(null, dbl = isFloating() ? Double.parseDouble(text) : 0.0d)
                ? dbl : floatingValue.get();
    }

    /**
     * Attempts to cast the token's text to a long and return it.
     * It will return 0 if the operation is not possible
     *
     * @return The Symbol's numeric value
     */
    public Long getIntegerValue() {
        Long n = integerValue.get();
        if (n != null) {
            return n;
        }
        return integerValue.compareAndSet(null, n = isInteger() ? Long.parseLong(text) : 0)
                ? n : integerValue.get();
    }

    @Override
    public String toString() {
        return Util.str("[line:%d, char:%d, Type:%s, Text:%s]", line, offset, type, text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Symbol that) {
            return type == that.type && text.equals(that.text);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return text.hashCode() * 11 + type.hashCode() * 17;
    }
}