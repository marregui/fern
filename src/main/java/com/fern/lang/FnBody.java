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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class FnBody<RT> {
  private final AtomicReference<IFn<RT>> fn;
  private final ThreadLocal<Stack> callStack;
  private final ThreadLocal<RecurData<RT>> recurData;
  private int arity;
  private boolean isVararg;
  
  public FnBody() {
    this.fn = new AtomicReference<>();
    this.callStack = new ThreadLocal<Stack>(){
      @Override
      protected Stack initialValue() {
        return new Stack();
      }
    };
    this.recurData = new ThreadLocal<RecurData<RT>>(){
      @Override
      protected RecurData<RT> initialValue() {
        return new RecurData<>();
      }
    };
  }
  
  private static class RecurData<RT> {
    private final AtomicReference<RT> lastResult;
    private final AtomicBoolean recurInvoked;

    RecurData () {
      this.recurInvoked = new AtomicBoolean();
      this.lastResult = new AtomicReference<>();
    }
    
    boolean recurInvoked() {
      return this.recurInvoked.getAndSet(true);
    }
    
    boolean recurInvoked(final RT result) {
      this.lastResult.set(result);
      return this.recurInvoked.getAndSet(false);
    }
    
    RT result() {
      return this.lastResult.get();
    }
  }
  
  final RT runFnBody () {
    RT result = null;
    do { result = fnBody(); } while (this.recurData.get().recurInvoked(result));
    return result;
  }
  
  final void pushArgs(final Object... args){
    this.callStack.get().push(args);
  }
  
  final void popArgs(){
    this.callStack.get().pop();
  }
  
  public final RT tailrecur(final Object... args) {
    final RecurData<RT> recurData = this.recurData.get();
    recurData.recurInvoked();
    this.callStack.get().replaceTop(args);
    return recurData.result();
  }
  
  public abstract RT fnBody();
  
  public final RT selfInvoke(final Object... args){
    return this.fn.get().invoke(args);
  }
  
  final FnBody<RT> setFn(final IFn<RT> fn, final int arity, final boolean isVararg) {
    this.fn.set(fn);
    this.arity = arity;
    this.isVararg = isVararg;
    return this;
  }
  
  public final int arity() {
    return this.arity;
  }
  
  public final boolean isVararg() {
    return this.isVararg;
  }
  
  public final int arglen(){
    return this.callStack.get().peek().length;
  }
  
  public final int vararglen(){
    return this.isVararg? this.callStack.get().peek().length - this.arity + 1 : 0;
  }
  
  @SuppressWarnings("unchecked")
  public final <T> T arg(final String name) {
    switch(name) {
      case "$#":    return (T) Integer.valueOf(arglen());
      case "$0":    return (T) arg(0);
      case "$1":    return (T) arg(1);
      case "$2":    return (T) arg(2);
      case "$3":    return (T) arg(3);
      case "$4":    return (T) arg(4);
      case "$5":    return (T) arg(5);
      case "$6":    return (T) arg(6);
      case "$7":    return (T) arg(7);
      case "$8":    return (T) arg(8);
      case "$9":    return (T) arg(9);
      case "$10":   return (T) arg(10);
      
      case "$$#":   return (T) Integer.valueOf(vararglen());
      case "$$0":   return (T) vararg(0);
      case "$$1":   return (T) vararg(1);
      case "$$2":   return (T) vararg(2);
      case "$$3":   return (T) vararg(3);
      case "$$4":   return (T) vararg(4);
      case "$$5":   return (T) vararg(5);
      case "$$6":   return (T) vararg(6);
      case "$$7":   return (T) vararg(7);
      case "$$8":   return (T) vararg(8);
      case "$$9":   return (T) vararg(9);
      case "$$10":  return (T) vararg(10);
    }
    if (false == name.startsWith("$")) {
      throw new IllegalArgumentException(name);
    }
    final int offset = name.startsWith("$$")? 2 : 1;
    if (name.length() > offset && allDigits(name.substring(offset))) {
      final int idx = Integer.parseInt(name.substring(offset));
      switch(offset) {
        case 1: return arg(idx);
        case 2: return vararg(idx);
      }
    }
    throw new IllegalArgumentException(name);
  }
  
  private static final boolean allDigits(final String digits) {
    for (int i=0; i < digits.length(); i++) {
      final int d = digits.charAt(i) - 48;
      if (d < 0 || d > 9) {
        return false;
      }
    }
    return digits.length() > 0;
  }
  
  public final Object [] args() {
    return arg(0);
  }
  
  @SuppressWarnings("unchecked")
  public final <T> T arg(final int idx) {
    final Object [] args = this.callStack.get().peek();
    if (idx == 0) {
      return (T) args;
    }
    if (idx >= 1 && idx <= args.length) {
      return (T) args[idx - 1];
    }
    throw new IndexOutOfBoundsException();
  }
  
  public final Object [] varargs() {
    return vararg(0);
  }
  
  @SuppressWarnings("unchecked")
  public final <T> T vararg(final int idx) {
    if (false == this.isVararg){
      throw new IllegalStateException("no varargs");
    }
    final Object [] args = this.callStack.get().peek();
    final int vararglen = args.length - this.arity + 1;
    if (idx == 0) {
      final Object [] varargs = new Object[vararglen];
      System.arraycopy(args, this.arity - 1, varargs, 0, varargs.length);
      return (T) varargs;
    }
    if (idx >= 1 && idx <= vararglen) {
      return (T) args[this.arity + idx - 2];
    }
    throw new IndexOutOfBoundsException();
  }
}