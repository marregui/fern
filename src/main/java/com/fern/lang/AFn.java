package com.fern.lang;

import static com.fern.util.Util.str;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class AFn<RT> implements IFn<RT> {
    private static final Object[] EMPTY_ARGS = new Object[]{ /* empty */};
    private static final int MAX_ARGS = 254; // number of 4 byte references minus 'this'
    private static final String UNDOCUMENTED = "No documentation available";
    private static final String ANONYMOUS_FN_NAME = "ANONYMOUS";
    private static final AtomicInteger UNIQUE_FN_ID = new AtomicInteger(0);

    private final String uniqueId;
    private final String name;
    private final String doc;
    private final Args defs;
    private Class<RT> retType;
    private final AtomicReference<FnBody<RT>> body;
    private final AtomicReference<String> quickStr;

    AFn(String name, String doc, Args defs, FnBody<RT> fnBody) {
        this(name, doc, defs, null, fnBody);
    }

    @SuppressWarnings("unchecked")
    AFn(String name, String doc, Args defs, Class<RT> retType, FnBody<RT> body) {
        if (defs == null) {
            throw new NullPointerException("defs");
        }
        if (defs.size() > MAX_ARGS) {
            throw new IllegalArgumentException(str("max arity %d exceeded", MAX_ARGS));
        }
        if (body == null) {
            throw new NullPointerException("fnBody");
        }
        this.name = (name != null) ? name : ANONYMOUS_FN_NAME;
        this.doc = (doc != null) ? doc : UNDOCUMENTED;
        this.defs = defs;
        this.body = new AtomicReference<>(body.setFn(this, defs.size(), defs.isLastArgVararg()));
        this.retType = (retType != null) ? retType : (Class<RT>) ((ParameterizedType) this.body.get().getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.uniqueId = String.format("fn-%d %s [%s] => %s",
                UNIQUE_FN_ID.getAndIncrement(),
                this.name,
                this.defs.moniker(),
                this.retType.getSimpleName());
        this.quickStr = new AtomicReference<>(null);
    }

    @Override
    public String uniqueId() {
        return uniqueId;
    }

    @Override
    public Class<RT> returnType() {
        return retType;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Args argDefs() {
        return defs;
    }

    @Override
    public String doc() {
        return doc;
    }

    @Override
    public final void run() {
        if (defs.size() != 0) {
            throw new IllegalArgumentException();
        }
        invoke(EMPTY_ARGS);
    }

    @Override
    public final RT call() throws Exception {
        if (defs.size() != 0) {
            throw new IllegalArgumentException();
        }
        return invoke(EMPTY_ARGS);
    }

    @Override
    public final RT invoke(Object... args) {
        FnBody<RT> fnBody = body.get();
        checkArity(args);
        fnBody.pushArgs(args);
        try {
            return fnBody.runFnBody();
        } finally {
            fnBody.popArgs();
        }
    }

    private final void checkArity(Object... args) {
        int arity = argDefs().size();
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
        } else {
            if (args.length != arity) {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public final String toString() {
        String str = quickStr.get();
        if (str == null) {
            synchronized (quickStr) {
                str = quickStr.get();
                if (str == null) {
                    int arity = defs.size();
                    StringBuilder sb = new StringBuilder();
                    sb.append("fn/").append(arity);
                    if (name != null) {
                        sb.append(" ").append(name);
                    }
                    sb.append(" (").append(defs).append(") -> ").append(retType.getName());
                    sb.append("\n").append(doc());
                    quickStr.set(sb.toString());
                }
            }
        }
        return quickStr.get();
    }
}