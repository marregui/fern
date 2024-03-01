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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ReversePolishNotationCalculator {
    private static final String DEF_OP = "def";
    private static final String DEL_OP = "del";
    private static final String ADD_OP = "+";
    private static final String SUBS_OP = "-";
    private static final String MULT_OP = "*";
    private static final String DIV_OP = "/";
    private static final Map<String, Integer> NUM_OPERANDS = new HashMap<>();

    static {
        NUM_OPERANDS.put(ADD_OP, 2);
        NUM_OPERANDS.put(SUBS_OP, 2);
        NUM_OPERANDS.put(MULT_OP, 2);
        NUM_OPERANDS.put(DIV_OP, 2);
        NUM_OPERANDS.put(DEF_OP, 2);
        NUM_OPERANDS.put(DEL_OP, 1);
    }

    private static final Map<String, String> SYMBOL_TABLE = new HashMap<>();

    private static boolean isOperator(String tok) {
        return null != tok && NUM_OPERANDS.keySet().contains(tok);
    }

    private static int numberOfOperands(String op) {
        return (null != op && NUM_OPERANDS.containsKey(op)) ? NUM_OPERANDS.get(op) : 0;
    }

    private static int resolve(String operand) throws Exception {
        try {
            return Integer.valueOf(operand);
        } catch (NumberFormatException nfe) {
            String symbol = SYMBOL_TABLE.get(operand);
            if (null == symbol) {
                System.out.printf("Symbol[%s] not defined\n", operand);
                throw nfe;
            }
            return resolve(symbol);
        }
    }

    private static String applyOperator(String op, List<String> operands) throws Exception {
        // Memory allocation operands
        switch (op) {
            case DEF_OP:
                SYMBOL_TABLE.put(operands.get(0), operands.get(1));
                System.out.printf("def %s <- %s\n", operands.get(0), operands.get(1));
                return null;
            case DEL_OP:
                SYMBOL_TABLE.remove(operands.get(0));
                System.out.printf("del %s\n", operands.get(0));
                return null;
        }

        // Numeric operands
        try {
            int r = -1;
            switch (op) {
                case ADD_OP:
                    r = 0;
                    for (String o : operands) {
                        r += resolve(o);
                    }
                    break;

                case SUBS_OP:
                    r = 0;
                    for (String o : operands) {
                        r -= resolve(o);
                    }
                    break;

                case MULT_OP:
                    r = 1;
                    for (String o : operands) {
                        r *= resolve(o);
                    }
                    break;

                case DIV_OP:
                    r = 1;
                    for (String o : operands) {
                        r /= resolve(o);
                    }
                    break;
            }
            System.out.printf("apply(%s: %s) -> %d\n", op, operands, r);
            return String.valueOf(r);
        } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
            System.out.printf("Invalid operand in stack\n");
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        String[] tokens = new String[]{"a", "7", "def", "e", "a", "3", "1", "+", "*", "def"};
        System.out.print("Tokens:");
        for (String tok : tokens) {
            System.out.printf(" %s", tok);
        }
        System.out.println("");

        Stack<String> stack = new Stack<>();
        for (String tok : tokens) {
            if (isOperator(tok)) {
                int n = numberOfOperands(tok);
                if (stack.size() < n) {
                    throw new Exception("Not enough operands on stack");
                }
                int offset = stack.size() - n;
                String result = applyOperator(tok, stack.subList(offset, stack.size()));
                stack.setSize(offset);
                if (null != result) {
                    stack.push(result);
                }
            } else {
                stack.push(tok);
                System.out.printf("Push: %s\n", tok);
            }
        }
        if (stack.size() > 1) {
            throw new Exception("User input has too many values");
        } else if (1 == stack.size()) {
            System.out.printf("Result: %s\n", stack.pop());
        }
    }
}