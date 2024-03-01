/**
 * Copyright (c) Miguel Arregui. All rights reserved.
 * <p>
 * The use and distribution terms for this software are covered by the
 * <p>
 * Apache License 2.0
 * (https://opensource.org/licenses/Apache-2.0)
 * <p>
 * available in the LICENSE file at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound
 * by the terms of this license. You must not remove this notice, or
 * any other, from this software.
 **/
package com.fern.lang;

import org.junit.Test;
import com.fern.BaseTest;
import com.fern.lang.FnBody;
import com.fern.lang.IFn;
import com.fern.seq.IHashed;
import com.fern.seq.ISeq;
import com.fern.seq.List;

import static com.fern.lang.Fn.*;
import static com.fern.seq.Colls.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class HighOrderTest extends BaseTest {
    @Test
    public void testGenerator() {
        final IFn<Integer> product = defn(defvarargs(int.class), new FnBody<Integer>() {
            @Override
            public Integer fnBody() {
                int prod = arg(1);
                for (int i = 2; i <= vararglen(); i++) {
                    final int n = arg(i);
                    prod *= n;
                }
                return prod;
            }
        });
        final IPredicate even = defpred(new FnBody<Boolean>() {
            @Override
            public Boolean fnBody() {
                for (int i = 1; i <= vararglen(); i++) {
                    final int n = arg(i);
                    if (n % 2 != 0) {
                        return false;
                    }
                }
                return true;
            }
        });

        final IPredicate odd = defpred(new FnBody<Boolean>() {
            @Override
            public Boolean fnBody() {
                for (int i = 1; i <= vararglen(); i++) {
                    final int n = arg(i);
                    if (n % 2 == 0) {
                        return false;
                    }
                }
                return true;
            }
        });
        assertEquals(newlist(12, 14, 24, 28, 36, 42), generator(product, range(1, 3), range(6, 7), newlist(2)));
        assertEquals(newlist(24), generator(product, even, range(1, 3), range(6, 7), newlist(2)));
        assertEquals(newlist(), generator(product, odd, range(1, 3), range(6, 7), newlist(2)));
        assertEquals(newlist(), generator(product));
    }

    @Test
    public void testReduce() {
        final IFn<Integer> intSum = defn(defargs(Integer.class, Integer.class), new FnBody<Integer>() {
            @Override
            public Integer fnBody() {
                final Integer n1 = arg("$1");
                final Integer n2 = arg("$2");
                return n1 + n2;
            }
        });
        final Integer r1 = reduce(intSum, 0, newlist(1, 2, 3, 4));
        final Integer r2 = reduce(intSum, newlist(1, 2, 3, 4));
        assertTrue(r1 == 10);
        assertTrue(r2 == 10);
        assertEquals(r1, r2);
        assertNull(reduce(intSum, newlist()));
        assertEquals(reduce(intSum, 10, newlist()), Integer.valueOf(10));
        assertEquals(reduce(intSum, newlist(10)), Integer.valueOf(10));
        assertEquals(reduce(intSum, newlist(9, 1)), Integer.valueOf(10));

        final IFn<IHashed> assocMap = defn(defargs(IHashed.class, ISeq.class), new FnBody<IHashed>() {
            @Override
            public IHashed fnBody() {
                final IHashed map = arg("$1");
                final ISeq item = arg("$2");
                return map.assoc(item.first(), item.last());
            }
        });
        final IHashed m = newmap(1, "one", 2, "two", 3, "three");
        assertEquals(reduce(assocMap, newmap(), newlist(newlist(1, "one"), newlist(2, "two"), newlist(3, "three"))), m);
        assertNull(reduce(assocMap, newlist()));
        assertEquals(reduce(assocMap, newmap(), newlist()), newmap());
    }

    @Test
    public void testMap() {
        final IFn<String> concat = defn(defvarargs(String.class), new FnBody<String>() {
            @Override
            public String fnBody() {
                final StringBuilder sb = new StringBuilder();
                final int argslen = vararglen();
                for (int i = 1; i <= argslen; i++) {
                    final String str = vararg(i);
                    sb.append(str);
                }
                return sb.toString();
            }
        });
        assertEquals(map(concat, newlist("A", "B", "C", "D"), newlist("1", "2", "3")), List.neu("A1", "B2", "C3"));
        assertEquals(map(concat, newlist("A", "B", "C"), newlist("1", "2", "3", "4")), List.neu("A1", "B2", "C3"));
        assertEquals(map(concat, newlist("A", "B", "C"), newlist("1", "2", "3")), List.neu("A1", "B2", "C3"));
        assertEquals(map(concat, newlist()), newlist());
        assertEquals(map(concat, newlist(), newlist(1)), newlist());

        final IFn<String> concat2 = defn(defargs(String.class, String.class), new FnBody<String>() {
            @Override
            public String fnBody() {
                final String a0 = arg("$1");
                final String a1 = arg("$2");
                return new StringBuilder(a0).append(a1).toString();
            }
        });
        assertEquals(map(concat2, newlist("A", "B", "C", "D"), newlist("1", "2", "3")), List.neu("A1", "B2", "C3"));
        assertEquals(map(concat2, newlist("A", "B", "C"), newlist("1", "2", "3", "4")), List.neu("A1", "B2", "C3"));
        assertEquals(map(concat2, newlist("A", "B", "C"), newlist("1", "2", "3")), List.neu("A1", "B2", "C3"));
        assertEquals(map(concat2, newlist()), newlist());
        assertEquals(map(concat2, newlist(), newlist(1)), newlist());
    }

    @Test
    public void testFilter() {
        final IFn<ISeq> range = defn(defargs(Integer.class, Integer.class, Integer.class), new FnBody<ISeq>() {
            @Override
            public ISeq fnBody() {
                final Integer start = arg("$1");
                final Integer end = arg("$2");
                final Integer step = arg("$3");
                ISeq range = List.neu();
                for (int i = start; i < end; i += step) {
                    range = range.cone(i);
                }
                return range;
            }
        });
        assertEquals(filter(even, range.invoke(0, 10, 1)), List.neu(0, 2, 4, 6, 8));
        assertEquals(filter(even, List.neu()), List.neu());
        assertEquals(filter(even, List.neu(3)), List.neu());
    }

    @Test
    public void testFilterXargs() {
        final IPredicate lessThanPivot = defpred(new FnBody<Boolean>() {
            @Override
            public Boolean fnBody() {
                return (int) arg("$1") < (int) arg("$2");
            }
        });
        assertEquals(filter(lessThanPivot, newlist(1, 2, 3, 4, 5, 6, 7, 8), 5), newlist(1, 2, 3, 4));
    }

    static final String str = "the brown fox jumps over the lazy dog";
    static final IFn<Integer> times3 = defn(defargs(Integer.class), new FnBody<Integer>() {
        @Override
        public Integer fnBody() {
            final Integer n = arg("$1");
            return 3 * n;
        }
    });

    @Test
    public void testCompose1() {
        final IFn<Integer> strlen = defn(defvarargs(String.class), new FnBody<Integer>() {
            @Override
            public Integer fnBody() {
                final Integer argslen = vararglen();
                int len = 0;
                for (int i = 1; i <= argslen; i++) {
                    final String str = vararg(i);
                    len += str != null ? str.length() : 0;
                }
                return len;
            }
        });
        final IFn<Integer> strlenX3 = compose(strlen, times3);
        assertEquals(strlenX3.invoke(str, "!"), Integer.valueOf((str.length() + 1) * 3));
    }

    @Test
    public void testCompose2() {
        final IFn<Integer> strlen = defn(defargs(String.class, String.class), new FnBody<Integer>() {
            @Override
            public Integer fnBody() {
                final String str1 = arg("$1");
                final String str2 = arg("$2");
                return (str1 != null ? str1.length() : 0) + (str2 != null ? str2.length() : 0);
            }
        });
        final IFn<Integer> strlenX3 = compose(strlen, times3);
        assertEquals(strlenX3.invoke(str, "!"), Integer.valueOf((str.length() + 1) * 3));
    }

    @Test
    public void testCompose3() {
        final IFn<Integer> strlen = defn(defvarargs(String.class, String.class), new FnBody<Integer>() {
            @Override
            public Integer fnBody() {
                final String str1 = arg("$1");
                final Integer argslen = vararglen();
                int len = 0;
                for (int i = 1; i <= argslen; i++) {
                    final String str = vararg(i);
                    len += str != null ? str.length() : 0;
                }
                return len + (str1 != null ? str1.length() : 0);
            }
        });
        final IFn<Integer> strlenX3 = compose(strlen, times3);
        assertEquals(strlenX3.invoke(str, "!", "!"), Integer.valueOf((str.length() + 2) * 3));
    }

    @Test
    public void testApply() {
        final IFn<Integer> strlen = defn(defvarargs(String.class, String.class), new FnBody<Integer>() {
            @Override
            public Integer fnBody() {
                final String str1 = arg("$1");
                final Integer argslen = vararglen();
                int len = 0;
                for (int i = 1; i <= argslen; i++) {
                    final String str = vararg(i);
                    len += str != null ? str.length() : 0;
                }
                return len + (str1 != null ? str1.length() : 0);
            }
        });
        final IFn<Integer> strlenX3 = compose(strlen, times3);
        assertEquals(apply(strlenX3, List.neu(str, "!", "!")), Integer.valueOf((str.length() + 2) * 3));
    }

    @Test
    public void testCurry() {
        final IFn<String> fn = defn(defargs(String.class, String.class, Integer.class), new FnBody<String>() {
            @Override
            public String fnBody() {
                final String str = arg("$1");
                final String sep = arg("$2");
                final Integer times = arg("$3");
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < times; i++) {
                    sb.append(str);
                    if (sep != null) {
                        sb.append(sep);
                    }
                }
                if (sep != null && times > 0) {
                    sb.setLength(sb.length() - sep.length());
                }
                return sb.toString();
            }
        });
        final IFn<String> cfn0 = curry(fn);
        final IFn<String> cfn1 = curry(fn, "*");
        final IFn<String> cfn2 = curry(fn, "*", null);
        final IFn<String> cfn3 = curry(fn, "*", null, 12);
        assertEquals(fn.invoke("*", null, 12), cfn0.invoke("*", null, 12));
        assertEquals(fn.invoke("*", null, 12), cfn1.invoke(null, 12));
        assertEquals(fn.invoke("*", null, 12), cfn2.invoke(12));
        assertEquals(fn.invoke("*", null, 12), cfn3.invoke());
    }

    @Test
    public void testCurryVarargs() {
        final IFn<String> fn = defn(defvarargs(int.class, String.class), new FnBody<String>() {
            @Override
            public String fnBody() {
                final int arglen = vararglen();
                final StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= arglen; i++) {
                    final String str = vararg(i);
                    sb.append(str);
                }
                final String concept = sb.toString();
                sb.setLength(0);
                final Integer times = arg("$1");
                for (int i = 0; i < times; i++) {
                    sb.append(concept);
                }
                return sb.toString();
            }
        });
        final IFn<String> cfn0 = curry(fn);
        final IFn<String> cfn1 = curry(fn, 3);
        final IFn<String> cfn2 = curry(fn, 3, "Alpha");
        final IFn<String> cfn3 = curry(fn, 3, "Alpha", "Beta");
        final IFn<String> cfn4 = curry(fn, 3, "Alpha", "Beta", "Gamma");
        final IFn<String> cfn5 = curry(fn, 3, "Alpha", "Beta", "Gamma", "Delta");
        assertEquals(fn.invoke(3, "Alpha", "Beta", "Gamma"), cfn0.invoke(3, "Alpha", "Beta", "Gamma"));
        assertEquals(fn.invoke(3, "Alpha", "Beta", "Gamma"), cfn1.invoke("Alpha", "Beta", "Gamma"));
        assertEquals(fn.invoke(3, "Alpha", "Beta", "Gamma"), cfn2.invoke("Beta", "Gamma"));
        assertEquals(fn.invoke(3, "Alpha", "Beta", "Gamma"), cfn3.invoke("Gamma"));
        assertEquals(fn.invoke(3, "Alpha", "Beta", "Gamma"), cfn4.invoke());
        assertEquals(fn.invoke(3, "Alpha", "Beta", "Gamma", "Delta", "Epsilon"), cfn5.invoke("Epsilon"));
    }
}