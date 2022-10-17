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
import net.jcip.annotations.Immutable;
import org.tinylog.configuration.Configuration;
import org.tinylog.format.AdvancedMessageFormatter;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.LoggingProvider;
import org.tinylog.provider.ProviderRegistry;
import org.tinylog.runtime.RuntimeProvider;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static elf4j.Level.*;

/**
 * Adapt tinylog capabilities to cater a JLF Logger
 */
@Immutable
class TinylogLogger implements Logger {
    private static final Level DEFAULT_LOG_LEVEL = INFO;
    private static final int INSTANCE_CALLER_DEPTH = 4;
    private static final EnumMap<Level, org.tinylog.Level> LEVEL_MAP = setLevelMap();
    private static final EnumMap<Level, Map<String, TinylogLogger>> LOGGER_CACHE = initLoggerCache();
    private static final int LOG_CALLER_DEPTH = 3;
    private static final MessageFormatter MESSAGE_FORMATTER =
            new AdvancedMessageFormatter(Configuration.getLocale(), Configuration.isEscapingEnabled());
    private static final LoggingProvider TINYLOG_PROVIDER = ProviderRegistry.getLoggingProvider();
    private static final org.tinylog.Level TINYLOG_PROVIDER_MINIMUM_LEVEL = TINYLOG_PROVIDER.getMinimumLevel();
    @NonNull private final String name;
    @NonNull private final Level level;

    private TinylogLogger(@NonNull String name, @NonNull Level level) {
        this.name = name;
        this.level = level;
    }

    static TinylogLogger instance() {
        return getLoggerByKey(RuntimeProvider.getCallerClassName(INSTANCE_CALLER_DEPTH), DEFAULT_LOG_LEVEL);
    }

    static TinylogLogger instance(Class<?> clazz) {
        return getLoggerByKey(
                clazz == null ? RuntimeProvider.getCallerClassName(INSTANCE_CALLER_DEPTH) : clazz.getName(),
                DEFAULT_LOG_LEVEL);
    }

    static TinylogLogger instance(String name) {
        return getLoggerByKey(name == null ? RuntimeProvider.getCallerClassName(INSTANCE_CALLER_DEPTH) : name,
                DEFAULT_LOG_LEVEL);
    }

    private static TinylogLogger getLoggerByKey(@NonNull String name, @NonNull Level level) {
        return LOGGER_CACHE.get(level).computeIfAbsent(name, k -> new TinylogLogger(k, level));
    }

    private static EnumMap<Level, Map<String, TinylogLogger>> initLoggerCache() {
        EnumMap<Level, Map<String, TinylogLogger>> loggerCache = new EnumMap<>(Level.class);
        EnumSet.allOf(Level.class).forEach(level -> loggerCache.put(level, new ConcurrentHashMap<>()));
        return loggerCache;
    }

    private static EnumMap<Level, org.tinylog.Level> setLevelMap() {
        EnumMap<Level, org.tinylog.Level> levelMap = new EnumMap<>(Level.class);
        levelMap.put(TRACE, org.tinylog.Level.TRACE);
        levelMap.put(DEBUG, org.tinylog.Level.DEBUG);
        levelMap.put(INFO, org.tinylog.Level.INFO);
        levelMap.put(WARN, org.tinylog.Level.WARN);
        levelMap.put(ERROR, org.tinylog.Level.ERROR);
        levelMap.put(OFF, org.tinylog.Level.OFF);
        return levelMap;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull Level getLevel() {
        return this.level;
    }

    @Override
    public Logger atLevel(Level level) {
        if (this.level == level) {
            return this;
        }
        return getLoggerByKey(this.name, level);
    }

    @Override
    public Logger atTrace() {
        return atLevel(TRACE);
    }

    @Override
    public Logger atDebug() {
        return atLevel(DEBUG);
    }

    @Override
    public Logger atInfo() {
        return atLevel(INFO);
    }

    @Override
    public Logger atWarn() {
        return atLevel(WARN);
    }

    @Override
    public Logger atError() {
        return atLevel(ERROR);
    }

    @Override
    public void log(Object message) {
        tinylog(null, message, null);
    }

    @Override
    public void log(Supplier<?> message) {
        tinylog(null, message, null);
    }

    @Override
    public void log(String message, Object... args) {
        tinylog(null, message, args);
    }

    @Override
    public void log(String message, Supplier<?>... args) {
        tinylog(null, message, args);
    }

    @Override
    public void log(Throwable t) {
        tinylog(t, null, null);
    }

    @Override
    public void log(Throwable t, String message) {
        tinylog(t, message, null);
    }

    @Override
    public void log(Throwable t, Supplier<String> message) {
        tinylog(t, message, null);
    }

    @Override
    public void log(Throwable t, String message, Object... args) {
        tinylog(t, message, args);
    }

    @Override
    public void log(Throwable t, String message, Supplier<?>... args) {
        tinylog(t, message, args);
    }

    private void tinylog(Throwable t, Object message, Object[] args) {
        org.tinylog.Level tinylogLevel = LEVEL_MAP.get(this.level);
        if (tinylogLevel.ordinal() < TINYLOG_PROVIDER_MINIMUM_LEVEL.ordinal() || this.level == OFF) {
            return;
        }
        if (message instanceof Supplier<?>) {
            message = ((Supplier<?>) message).get();
        }
        if (args instanceof Supplier<?>[]) {
            args = Arrays.stream(((Supplier<?>[]) args)).map(Supplier::get).toArray(Object[]::new);
        }
        TINYLOG_PROVIDER.log(LOG_CALLER_DEPTH,
                null,
                tinylogLevel,
                t,
                args == null ? null : MESSAGE_FORMATTER,
                message,
                args);
    }
}
