package com.fern.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.fern.util.Util.str;

public class Logger implements ILogger {
    private static final ConcurrentMap<Class<?>, ILogger> LOGGERS = new ConcurrentHashMap<>();
    private static final Level DEFAULT_LEVEL = Level.INFO;

    private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public static final ILogger loggerFor(Class<?> clazz) {
        ILogger logger = LOGGERS.get(clazz);
        if (logger == null) {
            ILogger other = LOGGERS.putIfAbsent(clazz, logger = new Logger());
            if (other != null) {
                logger = other;
            }
        }
        return logger;
    }

    private final AtomicReference<Level> level;

    private Logger() {
        level = new AtomicReference<>(DEFAULT_LEVEL);
    }

    @Override
    public void setLevel(Level newLevel) {
        level.set(newLevel);
    }

    @Override
    public Level getLevel() {
        return level.get();
    }

    @Override
    public void log(Level level, String format, Object... args) {
        if (getLevel().order() >= level.order()) {
            EXECUTOR.submit(() -> {
                String location = null;
                StringBuilder method = Util.THR_SB.get();
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 0; i < stack.length; i++) {
                    StackTraceElement e = stack[i];
                    String className = e.getClassName();
                    int j = className.length() - 1;
                    while (j > 0 && className.charAt(j) != '.') {
                        --j;
                    }
                    j++;

                    method.setLength(0);
                    method.append(className.substring(j)).append(".").append(e.getMethodName());
                    location = method.append("(l:").append(e.getLineNumber()).append(")").toString();
                    break;
                }
                System.out.print(str("%s <%d> Thr(%s) %s -> %s\n",
                        level,
                        TimeUnit.NANOSECONDS.toMicros(System.nanoTime()),
                        Thread.currentThread().getName(),
                        location,
                        str(format, args)));
            });
        }
    }

    @Override
    public final void debug(String format, Object... args) {
        log(Level.DEBUG, format, args);
    }

    @Override
    public final void info(String format, Object... args) {
        log(Level.INFO, format, args);
    }

    @Override
    public final void warn(String format, Object... args) {
        log(Level.WARN, format, args);
    }

    @Override
    public final void error(String format, Object... args) {
        log(Level.ERROR, format, args);
    }

    public static void main() {}
}