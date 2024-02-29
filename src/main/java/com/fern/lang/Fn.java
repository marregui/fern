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

import com.fern.seq.Colls;
import com.fern.seq.ISeq;
import com.fern.seq.List;
import static com.fern.util.Tools.str;
import java.util.Arrays;
import java.util.Iterator;

public final class Fn {
  private Fn() {
    throw new IllegalAccessError("this class comprises static utilities only");
  }
  
  // ==========================================
  // =    F U N C T I O N    F A C T O R Y    =
  // ==========================================
  public static Args defargs(final Class<?>... defs) {
    return new Args(false, defs);
  }
  
  public static Args defvarargs(final Class<?>... defs) {
    return new Args(true, defs);
  }
  
  public static final <RT> IFn<RT> defn(final FnBody<RT> fbBody){
    return new AFn<>(null, null, defargs(), fbBody);
  }
  
  public static final <RT> IFn<RT> defn(final Args args, final FnBody<RT> fnBody){
    return new AFn<>(null, null, args, fnBody);
  }
  
  public static final <RT> IFn<RT> defn(final String name, final FnBody<RT> fbBody){
    return new AFn<>(name, null, defargs(), fbBody);
  }
  
  public static final <RT> IFn<RT> defn(final String name, final Args args, final FnBody<RT> fnBody){
    return new AFn<>(name, null, args, fnBody);
  }
  
  public static final <RT> IFn<RT> defn(final String name, final String doc, final Args args, final FnBody<RT> fnBody){
    return new AFn<>(name, doc, args, fnBody);
  }
  
  // ===========================
  // =    P R E D I C A T E    =
  // ===========================
  public static final IPredicate defpred(final FnBody<Boolean> fnBody){
    return new AFn<>(null, null, defvarargs(Object.class), fnBody)::invoke;
  }
  
  // ===============================
  // =    C O M P O S I T I O N    =
  // ===============================
  @SuppressWarnings({ "unchecked" })
  public static final <RT> IFn<RT> compose(final IFn<?>... fns) {
    if (fns.length < 1) {
      throw new IllegalArgumentException("at least 1 fn is required");
    }
    final IFn<?> firstFn = fns[0];
    Class<?> prevFnReturnType = firstFn.returnType();
    for (int i=1; i < fns.length; i++) {
      final IFn<?> fn = fns[i];
      if (false == zeroth(fn).isAssignableFrom(prevFnReturnType)) {
        throw new IllegalArgumentException(str(
            "incompatible types, expected %s, got %s for fn %dth: %s", fn.argDefs(), prevFnReturnType, i, fn));
      }
      prevFnReturnType = fn.returnType();
    }
    return new AFn<>(null, null, firstFn.argDefs(), (Class<RT>) firstFn.returnType(), new FnBody<RT>(){
      @Override
      public RT fnBody() {
        Object result = fns[0].invoke(arg(0));
        for (int i=1; i < fns.length; i++) {
          final IFn<?> fn = fns[i];
          result = fn.invoke(result);
        }
        return (RT) result;
      }
    });
  }
  
  private static final Class<?> zeroth(final IFn<?> fn) {
    final Args argDefs = fn.argDefs();
    if (argDefs.size() == 1) {
      return argDefs.get(0);
    }
    throw new IllegalArgumentException(str("incompatible fn, expected arity == 1, got: %s", fn));
  }
  
  // ===================
  // =    A P P L Y    =
  // ===================
  public static final <RT> RT apply(final IFn<RT> fn, final ISeq argsSeq) {
    return fn.invoke(argsSeq.toArray());
  }
  
  // =========================
  // =    C U R R Y I N G    =
  // =========================
  public static final <RT> IFn<RT> curry(final IFn<RT> fn, final Object... args) {
    if (args.length == 0) {
      return fn;
    }
    if (false == fn.argDefs().isLastArgVararg()) {
      if (args.length > fn.argDefs().size()) {
        throw new IllegalArgumentException(str("too many args (%d) for fn: %s", args.length, fn));
      }
    }
    return new AFn<>(null, null, fn.argDefs().from(args.length), fn.returnType(), new FnBody<RT>(){
      @Override
      public RT fnBody() {
        final Object [] tailArgs = arg(0);
        final Object [] fullArgs = new Object[args.length + tailArgs.length];
        System.arraycopy(args, 0, fullArgs, 0, args.length);
        System.arraycopy(tailArgs, 0, fullArgs, args.length, tailArgs.length);
        return fn.invoke(fullArgs);
      }
    });
  }
  
  // =====================
  // =    R E D U C E    =
  // =====================
  public static final <RT> RT reduce(final IFn<RT> fn, final ISeq vals) {
    return reduce(fn, vals.first(), vals.rest());
  }

  @SuppressWarnings("unchecked")
  public static final <RT> RT reduce(final IFn<RT> fn, final Object initVal, final ISeq vals) {
    if (fn.argDefs().size() != 2) {
      throw new IllegalArgumentException(str("expected fn/2, got: %s", fn));
    }
    if (Colls.isNil(vals) || vals.size() == 0) {
      return (RT) initVal;
    }
    final Object [] array = vals.toArray();
    RT result = fn.invoke(initVal, array[0]);
    for (int i=1; i < array.length; i++) {
      result = fn.invoke(result, array[i]);
    }
    return result;
  }
  
  // ===============
  // =    M A P    =
  // ===============
  private static final int minSize(final ISeq... seqs) {
    int min = Integer.MAX_VALUE;
    for (ISeq seq: seqs) {
      min = Math.min(min, seq.size());
    }
    return min;
  }
  
  public static final ISeq map(final IFn<?> fn, final ISeq... seqs) {
    final int numberOfFnInvokes = minSize(seqs);
    if (seqs.length == 0 || numberOfFnInvokes == 0) {
      return new List();
    }
    final int arity = fn.argDefs().size();
    if ((arity == 1 && fn.argDefs().isLastArgVararg()) || arity == seqs.length) {
      final Object [] results = new Object[numberOfFnInvokes];
      final Object [] args = new Object[seqs.length];
      for (int argIdx=0; argIdx < numberOfFnInvokes; argIdx++) {
        for (int seqIdx=0; seqIdx < seqs.length; seqIdx++) {
          args[seqIdx] = seqs[seqIdx].nth(argIdx);
        }
        results[argIdx] = fn.invoke(args);
      }
      return new List(results);
    }
    throw new IllegalArgumentException(str("%s incompatible with |seqs| = %d", fn, seqs.length));
  }
  
  // =====================
  // =    F I L T E R    =
  // =====================
  public static final ISeq filter(final IPredicate pred, final ISeq seq) {
    return filter(pred, seq, null);
  }
  
  public static final ISeq filter(final IPredicate pred, final ISeq seq, final Object... xargs) {
    if (Colls.isNil(seq) || seq.size() == 0) {
      return new List();
    }
    final int xargsLen = (xargs != null)? xargs.length : 0;
    final Object [] extended = new Object[1 + xargsLen];
    if (xargsLen > 0) {
      System.arraycopy(xargs, 0, extended, 1, xargsLen);
    }
    final Object [] array = seq.toArray();
    final Object [] results = new Object[array.length];
    int offset = 0;
    for (int i=0; i < array.length; i++) {
      extended[0] = array[i];
      if (pred.isTrue(extended)) {
        results[offset++] = array[i];
      }
    }
    return new List(0, offset, results);
  } 
  
  // ===========================
  // =    G E N E R A T O R    =
  // ===========================
  public static final <RT> ISeq generator(final IFn<RT> fn, final ISeq... seqs) {
    return generator(fn, null, seqs);
  }
  
  public static final <RT> ISeq generator(final IFn<RT> fn, final IPredicate condPred, final ISeq... seqs) {
    if (seqs.length == 0) {
      return new List();
    }
    final int arity = fn.argDefs().size();
    if ((arity == 1 && fn.argDefs().isLastArgVararg()) || arity == seqs.length) {
      final Object [] results = new Object[resultsSize(seqs)];
      int offset = 0;
      final Object [] args = new Object[seqs.length];
      for (int [] idxs: LoopIdxs.of(seqs)) {
        for (int i=seqs.length-1, j=0; i >= 0; i--, j++) {
          args[j] = seqs[i].nth(idxs[j]);
        }
        if (condPred == null || condPred.isTrue(args)) {
          results[offset++] = fn.invoke(args);
        }
      }
      return new List(0, offset, results);
    }
    throw new IllegalArgumentException(str("%s incompatible with |seqs| = %d", fn, seqs.length));
  }
  
  private static final int resultsSize(final ISeq... seqs) {
    int resultsSize = seqs[0].size(); 
    for (int i=1; i < seqs.length; i++) {
      resultsSize *= seqs[i].size(); 
    }
    return resultsSize;
  }
  
  private static final class LoopIdxs implements Iterator<int[]>, Iterable<int[]> {
    static LoopIdxs of(final ISeq... seqs) {
      return new LoopIdxs(seqs);
    }
    
    private final int[] idxs;
    private final int[] maxIdxs;
    private boolean countEnded;

    private LoopIdxs(final ISeq... seqs) {
      this.maxIdxs = new int[seqs.length];
      for (int i=seqs.length-1, j=0; i >= 0; i--, j++) {
        this.maxIdxs[i] = seqs[j].size() - 1;
      }
      this.countEnded = false;
      this.idxs = new int[this.maxIdxs.length];
      Arrays.fill(this.idxs, 0);
      this.idxs[0] = -1;
    }
    
    @Override
    public Iterator<int[]> iterator() {
      return this;
    }
    
    @Override
    public boolean hasNext() {
      return false == this.countEnded;
    }
    
    @Override
    public int[] next() {
      if (false == this.countEnded) {
        next(0);
      }
      return this.idxs;
    }
    
    private void next(int i) {
      if (this.idxs[i] < this.maxIdxs[i]) {
        this.idxs[i]++;
        checkCountEnded();
      }
      else {
        this.idxs[i] = 0;
        next(i + 1);
      }
    }
    
    private boolean checkCountEnded() {
      if (false == this.countEnded) {
        boolean check = true;
        for (int i=0; i < this.idxs.length; i++) {
          if (this.idxs[i] != this.maxIdxs[i]) {
            check = false;
            break;
          }
        }
        this.countEnded = check;
      }
      return this.countEnded;
    }
  }
  
 // =====================
 // =    C O N C A T    =
 // =====================
 public static final ISeq concat(final Object... objs) {
   int size = 0;
   for (int i=0; i < objs.length; i++) {
     final Object e = objs[i]; 
     size += (e == null || false == e instanceof ISeq)? 1 : ((ISeq) e).size(); 
   }
   final Object [] result = new Object[size];
   int offset = 0;
   for (int i=0; i < objs.length; i++) {
     final Object e = objs[i];
     if (e == null || false == e instanceof ISeq) {
       result[offset++] = e;
       continue;
     }
     final ISeq seq = (ISeq) e;
     final Object [] array = seq.toArray();
     System.arraycopy(array, 0, result, offset, array.length);
     offset += array.length;
   }
   return new List(0, offset, result);
 }
 
 // =================
 // =    S O M E    =
 // =================
 public static final Object some(final IPredicate pred, final ISeq seq){
   return some(pred, seq, null);
 }
 
 public static final Object some(final IPredicate pred, final ISeq seq, final Object... xargs){
   if (Colls.isNil(seq) || seq.size() == 0) {
     return seq;
   }
   final int xargsLen = (xargs != null)? xargs.length : 0;
   final Object [] extended = new Object[1 + xargsLen];
   if (xargsLen > 0) {
     System.arraycopy(xargs, 0, extended, 1, xargsLen);
   }
   final Object [] array = seq.toArray();
   for (int i=0; i < array.length; i++) {
     extended[0] = array[i];
     if (pred.isTrue(extended)) {
       return array[i];
     }
   }
   return Colls.nil();
 }
 
 // ===================
 // =    R A N G E    =
 // ===================
 public static final ISeq range(final int end) {
   return range(0, end, 1);
 }
 
 public static final ISeq range(final int start, final int end) {
   return range(start, end, start < end? 1 : -1);
 }
 
 public static final ISeq range(final int start, final int end, final int step) {
   if (start == end) {
     return new List();
   }
   if ((start < end && step > 0) || (start > end && step < 0)) {
     final Object [] array = new Object[Math.abs(end - start) + 1];
     int offset = 0;
     if (start < end) {
       for (int i=start; i <= end; i+=step) {
         array[offset++] = i;
       }
     }
     else {
       for (int i=start; i >= end; i+=step) {
         array[offset++] = i;
       }
     }
     return new List(0, offset, array);
   }
   throw new IllegalArgumentException(str("start:%d, end:%d, step:%d", start, end, step));
 }
 
 // =================
 // =    T A K E    =
 // =================
 public static final ISeq take(final int n, final ISeq seq) {
   if (n < 0) {
     throw new IllegalArgumentException("n cannot be negative");
   }
   if (n == 0 || Colls.isNil(seq)) {
     return Colls.isNil(seq)? seq : new List();
   }
   if (n > seq.size()) {
     return seq;
   }
   final Object [] array = new Object[n];
   System.arraycopy(seq.toArray(), 0, array, 0, n);
   return new List(array);
 }
 
 public static final ISeq takewhile(final IPredicate pred, final ISeq seq) {
   return takewhile(pred, seq, null);
 }
 
 public static final ISeq takewhile(final IPredicate pred, final ISeq seq, final Object... xargs) {
   if (Colls.isNil(seq) || seq.isEmpty()) {
     return seq;
   }
   final int xargsLen = (xargs != null)? xargs.length : 0;
   final Object [] extended = new Object[1 + xargsLen];
   if (xargsLen > 0) {
     System.arraycopy(xargs, 0, extended, 1, xargsLen);
   }
   final Object [] array = seq.toArray();
   int offset = 0;
   for (int i=0; i < array.length; i++) {
     extended[0] = array[i];
     if (false == pred.isTrue(extended)) {
       break;
     }
     offset++;
   }
   return offset == 0? new List() : new List(0, offset, array);
 }

 // =================
 // =    D R O P    =
 // =================
 public static final ISeq drop(final int n, ISeq seq) {
   if (n < 0) {
     throw new IllegalArgumentException("n cannot be negative");
   }
   if (n == 0 || Colls.isNil(seq)) {
     return seq;
   }
   if (n >= seq.size()) {
     return new List();
   }
   final Object [] array = new Object[seq.size() - n];
   System.arraycopy(seq.toArray(), n, array, 0, array.length);
   return new List(array);
 }
 
 public static final ISeq dropwhile(final IPredicate pred, final ISeq seq) {
   return dropwhile(pred, seq, null);
 }
 
 public static final ISeq dropwhile(final IPredicate pred, final ISeq seq, final Object... xargs) {
   if (Colls.isNil(seq) || seq.isEmpty()) {
     return seq;
   }
   final int xargsLen = (xargs != null)? xargs.length : 0;
   final Object [] extended = new Object[1 + xargsLen];
   if (xargsLen > 0) {
     System.arraycopy(xargs, 0, extended, 1, xargsLen);
   }
   final Object [] array = seq.toArray();
   int offset = 0;
   for (int i=0; i < array.length; i++) {
     extended[0] = array[i];
     if (false == pred.isTrue(extended)) {
       break;
     }
     offset ++;
   }
   return offset == 0? seq : new List(offset, array.length, array);
 }
}