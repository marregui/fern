package com.fern.util;

public interface ILogger {
    enum Level {
        ERROR(0), WARN(1), INFO(2), DEBUG(3);

        private final int order;

        Level(int order) {
            this.order = order;
        }

        public int order() {
            return order;
        }
    }

    void setLevel(Level level);

    Level getLevel();

    void log(Level level, String format, Object... args);

    void debug(String format, Object... args);

    void info(String format, Object... args);

    void warn(String format, Object... args);

    void error(String format, Object... args);
}