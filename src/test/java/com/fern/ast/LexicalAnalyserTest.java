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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import com.fern.ast.Symbol.SymbolType;

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
                Symbol.get("", SymbolType.COMMENT),
                Symbol.get("nada", SymbolType.COMMENT),
                Symbol.get("albatros", SymbolType.COMMENT),
                Symbol.get("(", SymbolType.OPEN_P),
                Symbol.get("nada", SymbolType.COMMENT),
                Symbol.get(")", SymbolType.CLOSE_P),
                Symbol.get("to the end", SymbolType.COMMENT),
                Symbol.get("tic", SymbolType.COMMENT),
                Symbol.get("", SymbolType.COMMENT)
        );
    }

    @Test
    public void string() {
        String t = " (+ \"He;llo\" \"world\");) ";
        LexicalAnalyser.spit(t);
        check(t,
                Symbol.get("(", SymbolType.OPEN_P),
                Symbol.get("+", SymbolType.ARITHMETIC_OP),
                Symbol.get("He;llo", SymbolType.STRING),
                Symbol.get("world", SymbolType.STRING),
                Symbol.get(")", SymbolType.CLOSE_P),
                Symbol.get(") ", SymbolType.COMMENT)
        );
    }

    @Test(expected = RuntimeException.class)
    public void brokenString() {
        String t = "(+ \"Hi";
        check(t,
                Symbol.get("(", SymbolType.OPEN_P),
                Symbol.get("+", SymbolType.ARITHMETIC_OP)
        );
    }

    @Test
    public void numbers1() {
        String t = "1+2-1+14.+ 2 +2 -8 - 8 -8 -8. -8.3 2 .3 -8. 7.1";
        check(t,
                Symbol.get("1", SymbolType.INTEGER),
                Symbol.get("+2", SymbolType.INTEGER),
                Symbol.get("-1", SymbolType.INTEGER),
                Symbol.get("+14.", SymbolType.FLOATING),
                Symbol.get("+", SymbolType.ARITHMETIC_OP),
                Symbol.get("2", SymbolType.INTEGER),
                Symbol.get("+2", SymbolType.INTEGER),
                Symbol.get("-8", SymbolType.INTEGER),
                Symbol.get("-", SymbolType.ARITHMETIC_OP),
                Symbol.get("8", SymbolType.INTEGER),
                Symbol.get("-8", SymbolType.INTEGER),
                Symbol.get("-8.", SymbolType.FLOATING),
                Symbol.get("-8.3", SymbolType.FLOATING),
                Symbol.get("2", SymbolType.INTEGER),
                Symbol.get(".", SymbolType.DOT),
                Symbol.get("3", SymbolType.INTEGER),
                Symbol.get("-8.", SymbolType.FLOATING),
                Symbol.get("7.1", SymbolType.FLOATING)
        );
    }

    @Test
    public void numbers2() {
        String t = "8e-28e1e-1e-2 -1E- 2E2 2.E2+2.2E2";
        check(t,
                Symbol.get("8e-28", SymbolType.FLOATING),
                Symbol.get("e1e", SymbolType.REFERENCE),
                Symbol.get("-1e-2", SymbolType.FLOATING),
                Symbol.get("-1", SymbolType.INTEGER),
                Symbol.get("E", SymbolType.REFERENCE),
                Symbol.get("-", SymbolType.ARITHMETIC_OP),
                Symbol.get("2E2", SymbolType.FLOATING),
                Symbol.get("2.E2", SymbolType.FLOATING),
                Symbol.get("+2.2E2", SymbolType.FLOATING)
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
                    Symbol.get("!=", SymbolType.COMPARISON_OP),
                    Symbol.get("=", SymbolType.COMPARISON_OP),
                    Symbol.get(">", SymbolType.COMPARISON_OP),
                    Symbol.get("<", SymbolType.COMPARISON_OP),
                    Symbol.get(">=", SymbolType.COMPARISON_OP),
                    Symbol.get("<=", SymbolType.COMPARISON_OP)
            );
        }
    }

    @Test
    public void arithmetic() {
        for (String t : new String[]{"  - + * / % ", "-+*/%"}) {
            check(t,
                    Symbol.get("-", SymbolType.ARITHMETIC_OP),
                    Symbol.get("+", SymbolType.ARITHMETIC_OP),
                    Symbol.get("*", SymbolType.ARITHMETIC_OP),
                    Symbol.get("/", SymbolType.ARITHMETIC_OP),
                    Symbol.get("%", SymbolType.ARITHMETIC_OP)
            );
        }
    }

    @Test
    public void logic() {
        for (String t : new String[]{"  ! & | ", "!&|"}) {
            check(t,
                    Symbol.get("!", SymbolType.LOGIC_OP),
                    Symbol.get("&", SymbolType.LOGIC_OP),
                    Symbol.get("|", SymbolType.LOGIC_OP)
            );
        }
    }

    @Test
    public void dot() {
        for (String t : new String[]{"  . .   . ", "..."}) {
            check(t,
                    Symbol.get(".", SymbolType.DOT),
                    Symbol.get(".", SymbolType.DOT),
                    Symbol.get(".", SymbolType.DOT)
            );
        }
    }

    @Test
    public void reference() {
        String t = "  miguel arregui the cool)";
        check(t,
                Symbol.get("miguel", SymbolType.REFERENCE),
                Symbol.get("arregui", SymbolType.REFERENCE),
                Symbol.get("the", SymbolType.REFERENCE),
                Symbol.get("cool", SymbolType.REFERENCE),
                Symbol.get(")", SymbolType.CLOSE_P)
        );
    }

    @Test
    public void keyword() {
        String t = "  def new defdef;)";
        check(t,
                Symbol.get("def", SymbolType.KEYWORD),
                Symbol.get("new", SymbolType.KEYWORD),
                Symbol.get("defdef", SymbolType.REFERENCE),
                Symbol.get(")", SymbolType.COMMENT)
        );
    }

    private static void checkFloatingNumber(String text, Double expected) {
        assertEquals(LexicalAnalyser.getSymbols(text).get(0).getFloatingValue(), expected);
    }

    private static void check(String text, Symbol... expectedTokens) {
        int i = 0;
        for (Symbol token : LexicalAnalyser.get(SourceContent.fromText(text)).analyse().getSymbols()) {
            assertEquals(token, expectedTokens[i++]);
        }
    }
}