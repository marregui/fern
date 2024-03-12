package com.fern.util;

import java.lang.reflect.Array;

public class Util {

    public static final ThreadLocal<StringBuilder> THR_SB = new ThreadLocal<>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(255);
        }

        @Override
        public StringBuilder get() {
            StringBuilder sb = super.get();
            sb.setLength(0);
            return sb;
        }
    };

    public static final String str(String format, Object... args) {
        if (format == null) {
            return null;
        }

        int argsIdx = 0;
        StringBuilder sb = THR_SB.get();
        char[] formatBuff = format.toCharArray();
        char c;
        for (int i = 0, limit = formatBuff.length; i < limit; i++) {
            c = formatBuff[i];
            if (c != '%') {
                sb.append(c);
                continue;
            }

            // process %format
            i++;
            if (i < limit) {
                switch (formatBuff[i]) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case '%':
                        sb.append('%');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    default:
                        if (argsIdx < args.length) {
                            sb.append(args[argsIdx++]);
                        } else {
                            throw new IllegalArgumentException("missing arguments");
                        }
                }
            } else {
                throw new IllegalArgumentException("bad format");
            }
        }
        return sb.toString();
    }

    public static final boolean noe(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof String that) {
            return that.isEmpty();
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