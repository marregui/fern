package com.fern.seq;

import java.util.Comparator;

public final class Colls {
    // ===============================
    // =    N I L    /    T E S T    =
    // ===============================
    public static boolean isNil(ISeq seq) {
        return seq == ABaseSeq.NIL;
    }

    public static ISeq nil() {
        return ABaseSeq.NIL;
    }

    // ====================================
    // =    S E Q    F A C T O R I E S    =
    // ====================================
    public static ISeq newlist(Object... elements) {
        return List.neu(elements);
    }

    public static IHashed newset(Object... entries) {
        return Set.neu(entries);
    }

    public static IHashed newmap(Object... keyValPairs) {
        return Map.neu(keyValPairs);
    }

    // ===============================================
    // =    S E Q    A C C E S S    M E T H O D S    =
    // ===============================================
    public static int size(ISeq seq) {
        return seq.size();
    }

    public static boolean isEmpty(ISeq seq) {
        return seq.isEmpty();
    }

    public static Object first(ISeq seq) {
        return seq.first();
    }

    public static Object last(ISeq seq) {
        return seq.last();
    }

    public static ISeq rest(ISeq seq) {
        return seq.rest();
    }

    public static Object nth(int n, ISeq seq) {
        return seq.nth(n);
    }

    public static ISeq cons(Object e, ISeq seq) {
        return seq.cons(e);
    }

    public static ISeq cone(Object e, ISeq seq) {
        return seq.cone(e);
    }

    public static ISeq sorted(ISeq seq) {
        return seq.sorted();
    }

    public static ISeq sorted(ISeq seq, Comparator<Object> comparator) {
        return seq.sorted(comparator);
    }

    // =====================================================
    // =    H A S H E D    A C C E S S    M E T H O D S    =
    // =====================================================
    public static ISeq keys(IHashed hash) {
        return hash.keys();
    }

    public static ISeq values(IHashed hash) {
        return hash.values();
    }

    public static Object get(IHashed hash, Object key) {
        return hash.get(key);
    }

    public static boolean contains(IHashed hash, Object key) {
        return hash.contains(key);
    }

    public static IHashed assoc(IHashed hash, Object key, Object val) {
        return hash.assoc(key, val);
    }

    public static IHashed assoc(IHashed hash, Object key) {
        return hash.assoc(key);
    }
}