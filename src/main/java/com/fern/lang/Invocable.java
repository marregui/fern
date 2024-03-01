package com.fern.lang;

@FunctionalInterface
public interface Invocable<RT> {
    RT invoke(Object... args);
}