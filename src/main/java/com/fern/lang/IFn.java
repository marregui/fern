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

import java.util.concurrent.Callable;

public interface IFn<RT> extends Runnable, Callable<RT>, Invocable<RT> {
  String uniqueId();
  Class<RT> returnType();
  String name();
  String doc();
  Args argDefs();
}