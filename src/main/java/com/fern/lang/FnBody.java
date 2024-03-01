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
package com.fern.lang;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class FnBody<RT> {
    private final AtomicReference<IFn<RT>> fn;
    private final ThreadLocal<Stack> callStack;
    private final ThreadLocal<RecurData<RT>> recurData;
    private int arity;
    private boolean isVararg;

    public FnBody() {
        fn = new AtomicReference<>();
        callStack = ThreadLocal.withInitial(Stack::new);
        recurData = ThreadLocal.withInitial(RecurData::new);
    }

    private static class RecurData<RT> {
        private final AtomicReference<RT> lastResult;
        private final AtomicBoolean recurInvoked;

        RecurData() {
            recurInvoked = new AtomicBoolean();
            lastResult = new AtomicReference<>();
        }

        boolean recurInvoked() {
            return recurInvoked.getAndSet(true);
        }

        boolean recurInvoked(RT result) {
            lastResult.set(result);
            return recurInvoked.getAndSet(false);
        }

        RT result() {
            return lastResult.get();
        }
    }

    final RT runFnBody() {
        RT result;
        do {
            result = fnBody();
        } while (recurData.get().recurInvoked(result));
        return result;
    }

    final void pushArgs(Object... args) {
        callStack.get().push(args);
    }

    final void popArgs() {
        callStack.get().pop();
    }

    public final RT tailrecur(Object... args) {
        RecurData<RT> recurData = this.recurData.get();
        recurData.recurInvoked();
        callStack.get().replaceTop(args);
        return recurData.result();
    }

    public abstract RT fnBody();

    public RT selfInvoke(Object... args) {
        return fn.get().invoke(args);
    }

    FnBody<RT> setFn(IFn<RT> fn, int arity, boolean isVararg) {
        this.fn.set(fn);
        this.arity = arity;
        this.isVararg = isVararg;
        return this;
    }

    public int arity() {
        return arity;
    }

    public boolean isVararg() {
        return isVararg;
    }

    public int arglen() {
        return callStack.get().peek().length;
    }

    public int vararglen() {
        return isVararg ? callStack.get().peek().length - arity + 1 : 0;
    }

    @SuppressWarnings("unchecked")
    public <T> T arg(String name) {
        switch (name) {
            case "$#":
                return (T) Integer.valueOf(arglen());
            case "$0":
                return arg(0);
            case "$1":
                return arg(1);
            case "$2":
                return arg(2);
            case "$3":
                return arg(3);
            case "$4":
                return arg(4);
            case "$5":
                return arg(5);
            case "$6":
                return arg(6);
            case "$7":
                return arg(7);
            case "$8":
                return arg(8);
            case "$9":
                return arg(9);
            case "$10":
                return arg(10);

            case "$$#":
                return (T) Integer.valueOf(vararglen());
            case "$$0":
                return vararg(0);
            case "$$1":
                return vararg(1);
            case "$$2":
                return vararg(2);
            case "$$3":
                return vararg(3);
            case "$$4":
                return vararg(4);
            case "$$5":
                return vararg(5);
            case "$$6":
                return vararg(6);
            case "$$7":
                return vararg(7);
            case "$$8":
                return vararg(8);
            case "$$9":
                return vararg(9);
            case "$$10":
                return vararg(10);
        }
        if (false == name.startsWith("$")) {
            throw new IllegalArgumentException(name);
        }
        int offset = name.startsWith("$$") ? 2 : 1;
        if (name.length() > offset && allDigits(name.substring(offset))) {
            int idx = Integer.parseInt(name.substring(offset));
            switch (offset) {
                case 1:
                    return arg(idx);
                case 2:
                    return vararg(idx);
            }
        }
        throw new IllegalArgumentException(name);
    }

    private static boolean allDigits(String digits) {
        for (int i = 0; i < digits.length(); i++) {
            int d = digits.charAt(i) - 48;
            if (d < 0 || d > 9) {
                return false;
            }
        }
        return digits.length() > 0;
    }

    public Object[] args() {
        return arg(0);
    }

    @SuppressWarnings("unchecked")
    public <T> T arg(int idx) {
        Object[] args = callStack.get().peek();
        if (idx == 0) {
            return (T) args;
        }
        if (idx >= 1 && idx <= args.length) {
            return (T) args[idx - 1];
        }
        throw new IndexOutOfBoundsException();
    }

    public Object[] varargs() {
        return vararg(0);
    }

    @SuppressWarnings("unchecked")
    public <T> T vararg(int idx) {
        if (false == isVararg) {
            throw new IllegalStateException("no varargs");
        }
        Object[] args = callStack.get().peek();
        int vararglen = args.length - arity + 1;
        if (idx == 0) {
            Object[] varargs = new Object[vararglen];
            System.arraycopy(args, arity - 1, varargs, 0, varargs.length);
            return (T) varargs;
        }
        if (idx >= 1 && idx <= vararglen) {
            return (T) args[arity + idx - 2];
        }
        throw new IndexOutOfBoundsException();
    }
}