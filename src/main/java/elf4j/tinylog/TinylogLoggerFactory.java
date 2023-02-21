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
import java.util.concurrent.ConcurrentHashMap;

import static elf4j.Level.*;

/**
 * Provider class implementation of ELF4J SPI, loadable via {@link java.util.ServiceLoader}.
 *
 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html">Javadoc of
 *         ServiceLoader</a>
 */
public final class TinylogLoggerFactory implements LoggerFactory {
    static final EnumMap<Level, org.tinylog.Level> LEVEL_MAP;
    private static final Level DEFAULT_LOG_LEVEL = INFO;
    private static final int CALLER_DEPTH_NEW_INSTANCE = 7;
    private static final int CALLER_DEPTH_NEW_LEVEL = 7;
    private static final int CALLER_DEPTH_DEFAULT_NAME_INSTANCE = 4;

    static {
        LEVEL_MAP = new EnumMap<>(Level.class);
        LEVEL_MAP.put(TRACE, org.tinylog.Level.TRACE);
        LEVEL_MAP.put(DEBUG, org.tinylog.Level.DEBUG);
        LEVEL_MAP.put(INFO, org.tinylog.Level.INFO);
        LEVEL_MAP.put(WARN, org.tinylog.Level.WARN);
        LEVEL_MAP.put(ERROR, org.tinylog.Level.ERROR);
        LEVEL_MAP.put(OFF, org.tinylog.Level.OFF);
    }

    private final LoggingProvider loggingProvider;
    private final EnumMap<Level, Map<String, TinylogLogger>> loggerCache;

    /**
     * Default constructor required by {@link java.util.ServiceLoader}
     */
    public TinylogLoggerFactory() {
        this.loggingProvider = ProviderRegistry.getLoggingProvider();
        loggerCache = new EnumMap<>(Level.class);
        EnumSet.allOf(Level.class).forEach(level -> loggerCache.put(level, new ConcurrentHashMap<>()));
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

    TinylogLogger getLogger(final String name, final Level level) {
        String nameKey = (name == null) ? defaultLoggerName() : name;
        Level levelKey = (level == null) ? DEFAULT_LOG_LEVEL : level;
        return loggerCache.get(levelKey)
                .computeIfAbsent(nameKey,
                        key -> new TinylogLogger(nameKey,
                                levelKey,
                                loggingProvider.isEnabled(
                                        (level == null) ? CALLER_DEPTH_NEW_INSTANCE : CALLER_DEPTH_NEW_LEVEL,
                                        null,
                                        LEVEL_MAP.get(levelKey)),
                                this));
    }

    private static @NonNull String defaultLoggerName() {
        return new Throwable().getStackTrace()[CALLER_DEPTH_DEFAULT_NAME_INSTANCE].getClassName();
    }

    LoggingProvider getLoggingProvider() {
        return loggingProvider;
    }
}
