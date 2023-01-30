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
import elf4j.spi.LoggerFactory;
import lombok.NonNull;
import org.tinylog.provider.LoggingProvider;
import org.tinylog.provider.ProviderRegistry;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import static elf4j.Level.*;

/**
 * Provider class implementation of ELF4J SPI, loadable via {@link java.util.ServiceLoader}.
 *
 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html">Javadoc of
 *         ServiceLoader</a>
 */
public final class TinylogLoggerFactory implements LoggerFactory {
    static final EnumMap<Level, org.tinylog.Level> LEVEL_MAP = newLevelMap();
    private static final Level DEFAULT_LOG_LEVEL = INFO;
    private static final int NEW_INSTANCE_CALLER_DEPTH = 7;
    private static final int NEW_LEVEL_CALLER_DEPTH = 7;
    private final EnumMap<Level, Map<String, TinylogLogger>> loggerCache = newLoggerCache();
    private final LoggingProvider loggingProvider;
    private final org.tinylog.Level minimumTinyLogLevel;

    /**
     * Default constructor required by {@link java.util.ServiceLoader}
     */
    public TinylogLoggerFactory() {
        this.loggingProvider = ProviderRegistry.getLoggingProvider();
        this.minimumTinyLogLevel = this.loggingProvider.getMinimumLevel();
    }

    private static @NonNull String defaultLoggerName(StackTraceElement[] stackTraceElements) {
        int i = 0;
        String loggerInterfaceName = Logger.class.getName();
        for (; i < stackTraceElements.length; i++) {
            if (stackTraceElements[i].getClassName().equals(loggerInterfaceName)) {
                break;
            }
        }
        for (i++; i < stackTraceElements.length; i++) {
            StackTraceElement caller = stackTraceElements[i];
            if (!caller.getClassName().equals(loggerInterfaceName)) {
                return caller.getClassName();
            }
        }
        throw new NoSuchElementException();
    }

    private static @NonNull EnumMap<Level, org.tinylog.Level> newLevelMap() {
        EnumMap<Level, org.tinylog.Level> levelMap = new EnumMap<>(Level.class);
        levelMap.put(TRACE, org.tinylog.Level.TRACE);
        levelMap.put(DEBUG, org.tinylog.Level.DEBUG);
        levelMap.put(INFO, org.tinylog.Level.INFO);
        levelMap.put(WARN, org.tinylog.Level.WARN);
        levelMap.put(ERROR, org.tinylog.Level.ERROR);
        levelMap.put(OFF, org.tinylog.Level.OFF);
        return levelMap;
    }

    private static @NonNull EnumMap<Level, Map<String, TinylogLogger>> newLoggerCache() {
        EnumMap<Level, Map<String, TinylogLogger>> loggerCache = new EnumMap<>(Level.class);
        EnumSet.allOf(Level.class).forEach(level -> loggerCache.put(level, new ConcurrentHashMap<>()));
        return loggerCache;
    }

    @Override
    public Logger logger() {
        return getLogger(null, null);
    }

    @Override
    public Logger logger(@Nullable String name) {
        return getLogger(name, null);
    }

    @Override
    public Logger logger(@Nullable Class<?> clazz) {
        return getLogger(clazz == null ? null : clazz.getName(), null);
    }

    void evictCachedLoggers() {
        loggerCache.values().forEach(Map::clear);
    }

    void evictCachedLoggers(@NonNull String loggerNameStartPattern) {
        loggerCache.values()
                .forEach(levelOfLoggers -> levelOfLoggers.keySet()
                        .removeIf(loggerName -> loggerName.startsWith(loggerNameStartPattern)));
    }

    TinylogLogger getLogger(final String name, final Level level) {
        String eName = name == null ? defaultLoggerName(Thread.currentThread().getStackTrace()) : name;
        Level eLevel = level == null ? DEFAULT_LOG_LEVEL : level;
        return loggerCache.get(eLevel).computeIfAbsent(eName, key -> {
            org.tinylog.Level tLevel = LEVEL_MAP.get(eLevel);
            boolean enabled = tLevel != org.tinylog.Level.OFF && tLevel.ordinal() >= minimumTinyLogLevel.ordinal()
                    && loggingProvider.isEnabled(level == null ? NEW_INSTANCE_CALLER_DEPTH : NEW_LEVEL_CALLER_DEPTH,
                    null,
                    tLevel);
            return new TinylogLogger(eName, eLevel, enabled, this);
        });
    }

    LoggingProvider getLoggingProvider() {
        return loggingProvider;
    }
}
