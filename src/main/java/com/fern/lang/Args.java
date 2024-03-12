package com.fern.lang;

import com.fern.util.Util;

import static com.fern.util.Util.noe;

import java.util.Arrays;
import java.util.stream.Collectors;

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
        if (defs.length > 0) {
            StringBuilder sb = Util.THR_SB.get();
            sb.append(Arrays.stream(defs).map(Class::getSimpleName).collect(Collectors.joining(", ")));
            if (lastArgIsVararg) {
                sb.append("*");
            }
            return sb.toString();
        }
        return null;
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
        if (o instanceof Args that) {
            return lastArgIsVararg == that.lastArgIsVararg && Arrays.equals(defs, that.defs);
        }
        return false;
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
        StringBuilder sb = Util.THR_SB.get();
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