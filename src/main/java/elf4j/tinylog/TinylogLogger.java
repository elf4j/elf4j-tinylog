/*
 * MIT License
 *
 * Copyright (c) 2023 ELF4J
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
 */

package elf4j.tinylog;

import elf4j.Level;
import elf4j.Logger;
import lombok.NonNull;
import lombok.ToString;
import org.tinylog.format.LegacyMessageFormatter;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.LoggingProvider;
import org.tinylog.provider.ProviderRegistry;
import org.tinylog.runtime.RuntimeProvider;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static elf4j.Level.*;

/**
 * Adapt tinylog capabilities to cater an ELF4J Logger
 */
@Immutable
@ToString
final class TinylogLogger implements Logger {
    private static final LoggingProvider LOGGING_PROVIDER = ProviderRegistry.getLoggingProvider();
    private static final MessageFormatter MESSAGE_FORMATTER = new LegacyMessageFormatter();
    private static final EnumMap<Level, Map<String, TinylogLogger>> LOGGER_CACHE;
    private static final EnumMap<Level, org.tinylog.Level> LEVEL_MAP;
    private static final int CALLER_DEPTH_DEFAULT_LEVEL = 9;
    private static final int CALLER_DEPTH_NEW_LEVEL = 8;
    private static final int CALLER_DEPTH_LOG = 3;

    static {
        LEVEL_MAP = new EnumMap<>(Level.class);
        LEVEL_MAP.put(TRACE, org.tinylog.Level.TRACE);
        LEVEL_MAP.put(DEBUG, org.tinylog.Level.DEBUG);
        LEVEL_MAP.put(INFO, org.tinylog.Level.INFO);
        LEVEL_MAP.put(WARN, org.tinylog.Level.WARN);
        LEVEL_MAP.put(ERROR, org.tinylog.Level.ERROR);
        LEVEL_MAP.put(OFF, org.tinylog.Level.OFF);

        LOGGER_CACHE = new EnumMap<>(Level.class);
        EnumSet.allOf(Level.class).forEach(level -> LOGGER_CACHE.put(level, new ConcurrentHashMap<>()));
    }

    @NonNull private final String callerClassName;
    @NonNull private final Level level;
    @NonNull private final org.tinylog.Level tLevel;

    private final boolean enabled;

    TinylogLogger(@NonNull String callerClassName, @NonNull Level level, boolean enabled) {
        this.callerClassName = callerClassName;
        this.level = level;
        this.enabled = enabled;
        this.tLevel = translate(this.level);
    }

    static TinylogLogger getLogger(final String name, final Level level) {
        String nameKey = (name == null) ? RuntimeProvider.getCallerClassName(Logger.class.getName()) : name;
        Level levelKey = (level == null) ? translate(LOGGING_PROVIDER.getMinimumLevel(null)) : level;
        return LOGGER_CACHE.get(levelKey)
                .computeIfAbsent(nameKey,
                        key -> new TinylogLogger(nameKey, levelKey, isEnabled(level == null, translate(levelKey))));
    }

    private static boolean isEnabled(boolean defaultLevel, org.tinylog.Level tinylogLevel) {
        return LOGGING_PROVIDER.isEnabled(defaultLevel ? CALLER_DEPTH_DEFAULT_LEVEL : CALLER_DEPTH_NEW_LEVEL,
                null,
                tinylogLevel);
    }

    private static Object resolve(Object o) {
        return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
    }

    private static Level translate(org.tinylog.Level tinylogLevel) {
        return LEVEL_MAP.entrySet()
                .stream()
                .filter(e -> e.getValue() == tinylogLevel)
                .findAny()
                .orElseThrow(NoSuchElementException::new)
                .getKey();
    }

    private static org.tinylog.Level translate(Level elf4jLevel) {
        return LEVEL_MAP.get(elf4jLevel);
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
    public boolean isEnabled() {
        return this.enabled;
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

    public @NonNull String getCallerClassName() {
        return callerClassName;
    }

    private Logger atLevel(Level level) {
        if (this.level == level) {
            return this;
        }
        return getLogger(this.callerClassName, level);
    }

    private void tinylog(@Nullable final Throwable t, @Nullable final Object message, @Nullable final Object[] args) {
        if (!isEnabled()) {
            return;
        }
        LOGGING_PROVIDER.log(CALLER_DEPTH_LOG,
                null,
                this.tLevel,
                t,
                args == null ? null : MESSAGE_FORMATTER,
                resolve(message),
                args == null ? null : Arrays.stream(args).map(TinylogLogger::resolve).toArray());
    }
}
