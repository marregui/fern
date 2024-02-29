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
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class AFn<RT> implements IFn<RT> {
  private static final Object [] EMPTY_ARGS = new Object[]{ /* empty */ };
  private static final int MAX_ARGS = 254; // number of 4 byte references minus 'this'
  private static final String UNDOCUMENTED = "No documentation available";
  private static final String ANONYMOUS_FN_NAME = "ANONYMOUS";
  private static final AtomicInteger UNIQUE_FN_ID = new AtomicInteger(0);
  
  private final String uniqueId;
  private final String name;
  private final String doc;
  private final Args defs;
  private  Class<RT> retType;
  private final AtomicReference<FnBody<RT>> body;
  private final AtomicReference<String> quickStr;
  
  AFn(final String name, final String doc, final Args defs, final FnBody<RT> fnBody) {
    this(name, doc, defs, null, fnBody);
  }

  @SuppressWarnings("unchecked")
  AFn(final String name, final String doc, final Args defs, final Class<RT> retType, final FnBody<RT> body) {
    if (defs == null) {
      throw new NullPointerException("defs");
    }
    if (defs.size() > MAX_ARGS){
      throw new IllegalArgumentException(str("max arity %d exeeded", MAX_ARGS));
    }
    if (body == null) {
      throw new NullPointerException("fnBody");
    }
    this.name = (name != null)? name : ANONYMOUS_FN_NAME;
    this.doc = (doc != null)? doc : UNDOCUMENTED;
    this.defs = defs;
    this.body = new AtomicReference<>(body.setFn(this, defs.size(), defs.isLastArgVararg()));
    this.retType = (retType != null)? retType : (Class<RT>) ((ParameterizedType) this.body.get().getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    this.uniqueId = String.format("fn-%d %s [%s] => %s", 
        UNIQUE_FN_ID.getAndIncrement(), 
        this.name,
        this.defs.moniker(),
        this.retType.getSimpleName());
    this.quickStr = new AtomicReference<>(null);
  }
  
  @Override
  public String uniqueId(){
    return this.uniqueId;
  }
  
  @Override
  public Class<RT> returnType() {
    return this.retType;
  }
  
  @Override
  public String name() {
    return this.name;
  }

  @Override
  public Args argDefs() {
    return this.defs;
  }
  
  @Override
  public String doc() {
    return this.doc;
  }
  
  @Override
  public final void run() {
    if (this.defs.size() != 0) {
      throw new IllegalArgumentException();
    }
    invoke(EMPTY_ARGS);
  }
  
  @Override
  public final RT call() throws Exception {
    if (this.defs.size() != 0) {
      throw new IllegalArgumentException();
    }
    return invoke(EMPTY_ARGS);
  }

  @Override
  public final RT invoke(final Object... args) {
    final FnBody<RT> fnBody = this.body.get();
    checkArity(args);
    fnBody.pushArgs(args);
    try {
      return fnBody.runFnBody();
    }
    finally {
      fnBody.popArgs();
    }
  }
  
  private final void checkArity(final Object... args) {
    final int arity = argDefs().size();
    if (args == null) {
      if (arity == 1 && argDefs().isLastArgVararg()) {
        return;
      }
      throw new IllegalArgumentException();
    }
    if (argDefs().isLastArgVararg()) {
      if (args.length < arity - 1) {
        throw new IllegalArgumentException();
      }
    } 
    else {
      if (args.length != arity) {
        throw new IllegalArgumentException();
      }
    }
  }
  
  @Override
  public final String toString() {
    String str = this.quickStr.get();
    if (str == null) {
      synchronized (this.quickStr) {
        str = this.quickStr.get();
        if (str == null) {
          final int arity = this.defs.size();
          final StringBuilder sb = new StringBuilder();
          sb.append("fn/").append(arity);
          if (this.name != null) {
            sb.append(" ").append(this.name);
          }
          sb.append(" (").append(this.defs).append(") -> ").append(this.retType.getName());
          sb.append("\n").append(doc());
          this.quickStr.set(sb.toString());
        }
      }
    }
    return this.quickStr.get();
  }
}