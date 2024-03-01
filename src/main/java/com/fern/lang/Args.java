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

import static com.fern.util.Tools.noe;

import java.util.Arrays;

final class Args {
    private final Class<?>[] defs;
    private final boolean lastArgIsVararg;

    Args(boolean lastArgIsVararg, Class<?>... defs) {
        for (int i = 0; i < defs.length; i++) {
            if (noe(defs[i])) {
                throw new NullPointerException("nulls not allowed");
            }
        }
        this.defs = defs;
        this.lastArgIsVararg = lastArgIsVararg;
    }

    String moniker() {
        String argsDesc = "";
        if (defs.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < defs.length; i++) {
                Class<?> clazz = defs[i];
                sb.append(clazz.getSimpleName()).append(", ");
            }
            sb.setLength(sb.length() - 2);
            if (lastArgIsVararg) {
                sb.append("*");
            }
            argsDesc = sb.toString();
        }
        return argsDesc;
    }

    boolean isLastArgVararg() {
        return lastArgIsVararg;
    }

    int size() {
        return defs.length;
    }

    Class<?> get(int i) {
        if (i >= 0 && i < defs.length) {
            return defs[i];
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int hashCode() {
        return defs.hashCode() + (lastArgIsVararg ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || false == o instanceof Args) {
            return false;
        }
        Args that = (Args) o;
        return lastArgIsVararg == that.lastArgIsVararg && Arrays.equals(defs, that.defs);
    }

    Args from(int idx) {
        if (idx >= 0 && (lastArgIsVararg || idx <= size())) {
            if (lastArgIsVararg && idx >= size()) {
                return new Args(lastArgIsVararg, defs[defs.length - 1]);
            }
            Class<?>[] array = new Class<?>[defs.length - idx];
            System.arraycopy(defs, idx, array, 0, array.length);
            return new Args(lastArgIsVararg, array);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < defs.length; i++) {
            sb.append("$").append(i + 1).append(" ").append(defs[i]).append(", ");
        }
        if (defs.length > 0) {
            sb.setLength(sb.length() - 2);
            if (lastArgIsVararg) {
                sb.append("*");
            }
        }
        return sb.toString();
    }
}