package com.fern.lang;

@FunctionalInterface
public interface IPredicate {
    boolean isTrue(Object[] args);
}