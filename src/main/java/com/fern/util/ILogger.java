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