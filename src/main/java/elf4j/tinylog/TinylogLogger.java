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
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.tinylog.format.LegacyMessageFormatter;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.LoggingProvider;
import org.tinylog.provider.ProviderRegistry;
import org.tinylog.runtime.RuntimeProvider;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static elf4j.Level.*;

/**
 * Adapt tinylog capabilities to cater an ELF4J Logger
 */
@Immutable
@ToString
@EqualsAndHashCode
final class TinylogLogger implements Logger {
    private static final LoggingProvider LOGGING_PROVIDER = ProviderRegistry.getLoggingProvider();
    private static final MessageFormatter MESSAGE_FORMATTER = new LegacyMessageFormatter();
    private static final EnumMap<Level, Map<String, TinylogLogger>> LOGGER_CACHE;
    private static final Map<TinylogLogger, Boolean> ENABLEMENT_CACHE;
    private static final EnumMap<Level, org.tinylog.Level> TO_PROVIDER_LEVEL;
    private static final EnumMap<org.tinylog.Level, Level> FROM_PROVIDER_LEVEL;
    private static final int CALLER_DEPTH_ENABLED_CHECK = 5;
    private static final int CALLER_DEPTH_ENABLED_LOG = 6;
    private static final int CALLER_DEPTH_LOG = 3;

    static {
        TO_PROVIDER_LEVEL = new EnumMap<>(Level.class);
        TO_PROVIDER_LEVEL.put(TRACE, org.tinylog.Level.TRACE);
        TO_PROVIDER_LEVEL.put(DEBUG, org.tinylog.Level.DEBUG);
        TO_PROVIDER_LEVEL.put(INFO, org.tinylog.Level.INFO);
        TO_PROVIDER_LEVEL.put(WARN, org.tinylog.Level.WARN);
        TO_PROVIDER_LEVEL.put(ERROR, org.tinylog.Level.ERROR);
        TO_PROVIDER_LEVEL.put(OFF, org.tinylog.Level.OFF);

        FROM_PROVIDER_LEVEL = new EnumMap<>(org.tinylog.Level.class);
        FROM_PROVIDER_LEVEL.put(org.tinylog.Level.TRACE, TRACE);
        FROM_PROVIDER_LEVEL.put(org.tinylog.Level.DEBUG, DEBUG);
        FROM_PROVIDER_LEVEL.put(org.tinylog.Level.INFO, INFO);
        FROM_PROVIDER_LEVEL.put(org.tinylog.Level.WARN, WARN);
        FROM_PROVIDER_LEVEL.put(org.tinylog.Level.ERROR, ERROR);
        FROM_PROVIDER_LEVEL.put(org.tinylog.Level.OFF, OFF);

        LOGGER_CACHE = new EnumMap<>(Level.class);
        EnumSet.allOf(Level.class).forEach(level -> LOGGER_CACHE.put(level, new ConcurrentHashMap<>()));

        ENABLEMENT_CACHE = new ConcurrentHashMap<>();
    }

    @EqualsAndHashCode.Include @NonNull private final String callerClassName;
    @EqualsAndHashCode.Include @NonNull private final Level level;
    @NonNull private final org.tinylog.Level tinylogLevel;

    TinylogLogger(@NonNull String callerClassName, @NonNull Level level) {
        this.callerClassName = callerClassName;
        this.level = level;
        this.tinylogLevel = translate(this.level);
    }

    static TinylogLogger instance(final String name, final Level level) {
        String nameKey = (name == null) ? RuntimeProvider.getCallerClassName(Logger.class.getName()) : name;
        Level levelKey = (level == null) ? translate(LOGGING_PROVIDER.getMinimumLevel(null)) : level;
        return LOGGER_CACHE.get(levelKey).computeIfAbsent(nameKey, key -> new TinylogLogger(nameKey, levelKey));
    }

    private static Object resolve(Object o) {
        return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
    }

    private static Level translate(org.tinylog.Level tinylogLevel) {
        return FROM_PROVIDER_LEVEL.get(tinylogLevel);
    }

    private static org.tinylog.Level translate(Level elf4jLevel) {
        return TO_PROVIDER_LEVEL.get(elf4jLevel);
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
        return ENABLEMENT_CACHE.computeIfAbsent(this,
                logger -> LOGGING_PROVIDER.isEnabled(CALLER_DEPTH_ENABLED_CHECK, null, logger.tinylogLevel));
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
        return instance(this.callerClassName, level);
    }

    private void tinylog(@Nullable final Throwable exception,
            @Nullable final Object message,
            @Nullable final Object[] args) {
        if (Boolean.FALSE.equals((ENABLEMENT_CACHE.computeIfAbsent(this,
                logger -> LOGGING_PROVIDER.isEnabled(CALLER_DEPTH_ENABLED_LOG, null, logger.tinylogLevel))))) {
            return;
        }
        LOGGING_PROVIDER.log(CALLER_DEPTH_LOG,
                null,
                this.tinylogLevel,
                exception,
                args == null ? null : MESSAGE_FORMATTER,
                resolve(message),
                args == null ? null : Arrays.stream(args).map(TinylogLogger::resolve).toArray());
    }
}
