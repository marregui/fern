package com.fern.lang;

import static com.fern.util.Tools.str;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class NS {
    private static final Map<String, ConcurrentMap<String, IFn<?>>> NAMESPACES = new HashMap<>();

    public static final String GLOBAL_NS = "azji-global-ns";
    private static final String SEP = "/";

    private static String fnKey(String ns, IFn<?> fn) {
        return new StringBuilder(ns).append(SEP).append(fn.uniqueId()).toString();
    }

    public static String regfn(IFn<?> fn) {
        return regfn(GLOBAL_NS, fn);
    }

    public static String regfn(String ns, IFn<?> fn) {
        ConcurrentMap<String, IFn<?>> nsfns = null;
        synchronized (NAMESPACES) {
            nsfns = NAMESPACES.get(ns);
            if (nsfns == null) {
                NAMESPACES.putIfAbsent(ns, nsfns = new ConcurrentHashMap<>());
            }
        }
        String uniqueId = fn.uniqueId();
        if (null != nsfns.putIfAbsent(uniqueId, fn)) {
            throw new IllegalAccessError(str("NS(%s) already contains: %s", ns, uniqueId));
        }
        return fnKey(ns, fn);
    }

    public static <T> IFn<T> fn(String fnKey) {
        if (fnKey.contains(SEP)) {
            String[] parts = fnKey.split(SEP);
            if (2 == parts.length) {
                ConcurrentMap<String, IFn<?>> nsfns = NAMESPACES.get(parts[0]);
                if (null != nsfns) {
                    @SuppressWarnings("unchecked") IFn<T> fn = (IFn<T>) nsfns.get(parts[1]);
                    if (null != fn) {
                        return fn;
                    }
                }
            }
        }
        throw new IllegalAccessError(str("Not a valid key: %s", fnKey));
    }
}