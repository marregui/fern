/**
 * Copyright (c) Miguel Arregui. All rights reserved.
 * 
 * The use and distribution terms for this software are covered by the
 * 
 * Apache License 2.0
 * (https://opensource.org/licenses/Apache-2.0)
 * 
 * available in the LICENSE file at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound
 * by the terms of this license. You must not remove this notice, or
 * any other, from this software.
 **/
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
  
  private static final String fnKey(final String ns, final IFn<?> fn) {
    return new StringBuilder(ns).append(SEP).append(fn.uniqueId()).toString();
  }

  public final static String regfn(final IFn<?> fn) {
    return regfn(GLOBAL_NS, fn);
  }

  public final static String regfn(final String ns, final IFn<?> fn) {
    ConcurrentMap<String, IFn<?>> nsfns = null;
    synchronized (NAMESPACES) {
      nsfns = NAMESPACES.get(ns);
      if (nsfns == null) {
        NAMESPACES.putIfAbsent(ns, nsfns = new ConcurrentHashMap<>());
      }
    }
    final String uniqueId = fn.uniqueId();
    if (null != nsfns.putIfAbsent(uniqueId, fn)){
      throw new IllegalAccessError(str("NS(%s) already contains: %s", ns, uniqueId));
    }
    return fnKey(ns, fn);
  }
  
  public final static <T> IFn<T> fn(final String fnKey) {
    if (fnKey.contains(SEP)) {
      final String [] parts = fnKey.split(SEP);
      if (2 == parts.length) {
        final ConcurrentMap<String, IFn<?>> nsfns = NAMESPACES.get(parts[0]);
        if (null != nsfns) {
          @SuppressWarnings("unchecked")
          final IFn<T> fn = (IFn<T>) nsfns.get(parts[1]);
          if (null != fn) {
            return fn;
          }
        }
      }
    }
    throw new IllegalAccessError(str("Not a valid key: %s", fnKey));
  }
}