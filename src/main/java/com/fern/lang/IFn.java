package com.fern.lang;

import java.util.concurrent.Callable;

public interface IFn<RT> extends Runnable, Callable<RT>, Invocable<RT> {
    String uniqueId();

    Class<RT> returnType();

    String name();

    String doc();

    Args argDefs();
}