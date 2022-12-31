/*
 * MIT License
 *
 * Copyright (c) 2022 elf4j-tinylog
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package elf4j.tinylog;

import elf4j.Level;
import elf4j.Logger;
import lombok.NonNull;
import lombok.ToString;
import net.jcip.annotations.ThreadSafe;
import org.tinylog.format.LegacyMessageFormatter;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.LoggingProvider;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Supplier;

import static elf4j.Level.*;

/**
 * Adapt tinylog capabilities to cater an ELF4J Logger
 */
@ThreadSafe
@ToString
final class TinylogLogger implements Logger {
    private static final int EXTERNAL_CHECK_DEPTH = 3;
    private static final int EXTERNAL_LOG_DEPTH = 3;
    private static final int INTERNAL_CHECK_DEPTH = 4;
    private static final MessageFormatter MESSAGE_FORMATTER = new LegacyMessageFormatter();
    @NonNull private final org.tinylog.Level globalMminimumLevel;
    @NonNull private final Level level;
    @NonNull private final LoggingProvider loggingProvider;
    @NonNull private final String name;
    @NonNull private final TinylogLoggerFactory tinylogLoggerFactory;
    private volatile Boolean enabled;

    TinylogLogger(@NonNull String name, @NonNull Level level, @NonNull TinylogLoggerFactory tinylogLoggerFactory) {
        this.name = name;
        this.level = level;
        this.tinylogLoggerFactory = tinylogLoggerFactory;
        this.loggingProvider = tinylogLoggerFactory.getLoggingProvider();
        this.globalMminimumLevel = this.loggingProvider.getMinimumLevel();
    }

    private static Object resolve(Object o) {
        return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
    }

    @Override
    public Logger atDebug() {
        return atLevel(DEBUG);
    }

    @Override
    public Logger atError() {
        return atLevel(ERROR);
    }

    @Override
    public Logger atInfo() {
        return atLevel(INFO);
    }

    @Override
    public Logger atTrace() {
        return atLevel(TRACE);
    }

    @Override
    public Logger atWarn() {
        return atLevel(WARN);
    }

    @Override
    public @NonNull Level getLevel() {
        return this.level;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return checkEnabled(EXTERNAL_CHECK_DEPTH);
    }

    @Override
    public void log(Object message) {
        tinylog(null, message, null);
    }

    @Override
    public void log(String message, Object... args) {
        tinylog(null, message, args);
    }

    @Override
    public void log(Throwable t) {
        tinylog(t, null, null);
    }

    @Override
    public void log(Throwable t, Object message) {
        tinylog(t, message, null);
    }

    @Override
    public void log(Throwable t, String message, Object... args) {
        tinylog(t, message, args);
    }

    private Logger atLevel(Level level) {
        if (this.level == level) {
            return this;
        }
        return tinylogLoggerFactory.getLogger(this.name, level);
    }

    private boolean checkEnabled(int apiCallerDepth) {
        if (this.enabled == null) {
            org.tinylog.Level tLevel = TinylogLoggerFactory.LEVEL_MAP.get(this.level);
            this.enabled =
                    this.level != OFF && tLevel.ordinal() >= globalMminimumLevel.ordinal() && loggingProvider.isEnabled(
                            apiCallerDepth,
                            null,
                            tLevel);
        }
        return this.enabled;
    }

    @NonNull TinylogLoggerFactory getTinylogLoggerFactory() {
        return tinylogLoggerFactory;
    }

    private void tinylog(@Nullable final Throwable t, @Nullable final Object message, @Nullable final Object[] args) {
        if (!this.checkEnabled(INTERNAL_CHECK_DEPTH)) {
            return;
        }
        loggingProvider.log(EXTERNAL_LOG_DEPTH,
                null,
                TinylogLoggerFactory.LEVEL_MAP.get(this.level),
                t,
                args == null ? null : MESSAGE_FORMATTER,
                resolve(message),
                args == null ? null : Arrays.stream(args).map(TinylogLogger::resolve).toArray());
    }
}
