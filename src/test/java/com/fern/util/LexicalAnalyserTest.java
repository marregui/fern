package com.fern.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.fern.util.Symbol.Type;

public class LexicalAnalyserTest {
    @Test
    public void comments() {
        String t = ";\n";
        t += ";nada\n";
        t += ";albatros;\n";
        t += "  (  ; nada; )";
        t += "    ;   to the end";
        t += ";;; ;;; tic;;;  ;;\n";
        check(t,
                Symbol.create("", Type.COMMENT),
                Symbol.create("nada", Type.COMMENT),
                Symbol.create("albatros", Type.COMMENT),
                Symbol.create("(", Type.OPEN_P),
                Symbol.create("nada", Type.COMMENT),
                Symbol.create(")", Type.CLOSE_P),
                Symbol.create("to the end", Type.COMMENT),
                Symbol.create("tic", Type.COMMENT),
                Symbol.create("", Type.COMMENT)
        );
    }

    @Test
    public void string() {
        String t = " (+ \"He;llo\" \"world\");) ";
        check(t,
                Symbol.create("(", Type.OPEN_P),
                Symbol.create("+", Type.ARITHMETIC_OP),
                Symbol.create("He;llo", Type.STRING),
                Symbol.create("world", Type.STRING),
                Symbol.create(")", Type.CLOSE_P),
                Symbol.create(") ", Type.COMMENT)
        );
    }

    @Test(expected = RuntimeException.class)
    public void brokenString() {
        String t = "(+ \"Hi";
        check(t,
                Symbol.create("(", Type.OPEN_P),
                Symbol.create("+", Type.ARITHMETIC_OP)
        );
    }

    @Test
    public void numbers1() {
        String t = "1+2-1+14.+ 2 +2 -8 - 8 -8 -8. -8.3 2 .3 -8. 7.1";
        check(t,
                Symbol.create("1", Type.INTEGER),
                Symbol.create("+2", Type.INTEGER),
                Symbol.create("-1", Type.INTEGER),
                Symbol.create("+14.", Type.FLOATING),
                Symbol.create("+", Type.ARITHMETIC_OP),
                Symbol.create("2", Type.INTEGER),
                Symbol.create("+2", Type.INTEGER),
                Symbol.create("-8", Type.INTEGER),
                Symbol.create("-", Type.ARITHMETIC_OP),
                Symbol.create("8", Type.INTEGER),
                Symbol.create("-8", Type.INTEGER),
                Symbol.create("-8.", Type.FLOATING),
                Symbol.create("-8.3", Type.FLOATING),
                Symbol.create("2", Type.INTEGER),
                Symbol.create(".", Type.DOT),
                Symbol.create("3", Type.INTEGER),
                Symbol.create("-8.", Type.FLOATING),
                Symbol.create("7.1", Type.FLOATING)
        );
    }

    @Test
    public void numbers2() {
        String t = "8e-28e1e-1e-2 -1E- 2E2 2.E2+2.2E2";
        check(t,
                Symbol.create("8e-28", Type.FLOATING),
                Symbol.create("e1e", Type.REFERENCE),
                Symbol.create("-1e-2", Type.FLOATING),
                Symbol.create("-1", Type.INTEGER),
                Symbol.create("E", Type.REFERENCE),
                Symbol.create("-", Type.ARITHMETIC_OP),
                Symbol.create("2E2", Type.FLOATING),
                Symbol.create("2.E2", Type.FLOATING),
                Symbol.create("+2.2E2", Type.FLOATING)
        );
    }

    @Test
    public void numbersValues() {
        checkFloatingNumber("+2.2E2", Double.valueOf(220.0));
        checkFloatingNumber("-2.E-02", Double.valueOf(-0.02));
        checkFloatingNumber("-2.17E-001", Double.valueOf(-0.217));
        checkFloatingNumber("214E-1", Double.valueOf(21.4));
        checkFloatingNumber("214E6", Double.valueOf(214000000));
        checkFloatingNumber("4.", Double.valueOf(4.0));
        checkFloatingNumber("16.17", Double.valueOf(16.17));
    }

    @Test
    public void comparison() {
        for (String t : new String[]{"  != = > < >= <=", "!==><>=<="}) {
            check(t,
                    Symbol.create("!=", Type.COMPARISON_OP),
                    Symbol.create("=", Type.COMPARISON_OP),
                    Symbol.create(">", Type.COMPARISON_OP),
                    Symbol.create("<", Type.COMPARISON_OP),
                    Symbol.create(">=", Type.COMPARISON_OP),
                    Symbol.create("<=", Type.COMPARISON_OP)
            );
        }
    }

    @Test
    public void arithmetic() {
        for (String t : new String[]{"  - + * / % ", "-+*/%"}) {
            check(t,
                    Symbol.create("-", Type.ARITHMETIC_OP),
                    Symbol.create("+", Type.ARITHMETIC_OP),
                    Symbol.create("*", Type.ARITHMETIC_OP),
                    Symbol.create("/", Type.ARITHMETIC_OP),
                    Symbol.create("%", Type.ARITHMETIC_OP)
            );
        }
    }

    @Test
    public void logic() {
        for (String t : new String[]{"  ! & | ", "!&|"}) {
            check(t,
                    Symbol.create("!", Type.LOGIC_OP),
                    Symbol.create("&", Type.LOGIC_OP),
                    Symbol.create("|", Type.LOGIC_OP)
            );
        }
    }

    @Test
    public void dot() {
        for (String t : new String[]{"  . .   . ", "..."}) {
            check(t,
                    Symbol.create(".", Type.DOT),
                    Symbol.create(".", Type.DOT),
                    Symbol.create(".", Type.DOT)
            );
        }
    }

    @Test
    public void reference() {
        String t = "  miguel arregui the cool)";
        check(t,
                Symbol.create("miguel", Type.REFERENCE),
                Symbol.create("arregui", Type.REFERENCE),
                Symbol.create("the", Type.REFERENCE),
                Symbol.create("cool", Type.REFERENCE),
                Symbol.create(")", Type.CLOSE_P)
        );
    }

    @Test
    public void keyword() {
        String t = "  def new defdef;)";
        check(t,
                Symbol.create("def", Type.KEYWORD),
                Symbol.create("new", Type.KEYWORD),
                Symbol.create("defdef", Type.REFERENCE),
                Symbol.create(")", Type.COMMENT)
        );
    }

    private static void checkFloatingNumber(String text, Double expected) {
        assertEquals(LexicalAnalyser.extractSymbols(text).get(0).getFloatingValue(), expected);
    }

    private static void check(String text, Symbol... expectedTokens) {
        int i = 0;
        for (Symbol token : LexicalAnalyser.get(TextSource.create(text)).analyse().extractSymbols()) {
            assertEquals(token, expectedTokens[i++]);
        }
    }
}