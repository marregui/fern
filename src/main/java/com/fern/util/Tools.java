package com.fern.util;

import java.lang.reflect.Array;

public class Tools {

    public static final String str(String format, Object... args) {
        return String.format(format, args);
    }

    public static final String str(Object... args) {
        if (args == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("|").append(args.length).append("| -> ");
        for (int i = 0; i < args.length; i++) {
            sb.append("[").append(i).append("]: ").append(args[i]);
            if (args[i] != null) {
                sb.append(" (").append(args[i].getClass().getName()).append(")");
            }
            sb.append(", ");
        }
        if (args.length > 0) {
            sb.setLength(sb.length() - 2);
        }
        return String.format(sb.toString(), args);
    }

    public static final boolean noe(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof String) {
            return ((String) o).isEmpty();
        }
        if (o.getClass().isArray()) {
            return Array.getLength(o) == 0;
        }
        return false;
    }

    public static final int safeLen(Object... elements) {
        return elements != null ? Array.getLength(elements) : -1;
    }
}