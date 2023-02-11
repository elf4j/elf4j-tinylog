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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static elf4j.Level.*;
import static org.junit.jupiter.api.Assertions.*;

class TinylogLoggerTest {

    @Nested
    class Placeholder {
        Logger logger = Logger.instance(Placeholder.class);

        @Test
        void cacheLoggerWithSameNameAndLevel() {
            Logger error = logger.atError();
            Logger error2 = logger.atError();
            assertSame(error, error2, "logger instance of the same name and level is cached and re-used");
        }

        @Test
        void defaultNameAndLevel() {
            logger.log("no-arg logger instance {} in class {} has default logger name: {}, and default level: {}",
                    logger,
                    this.getClass().getName(),
                    logger.getName(),
                    logger.getLevel());
            assertEquals(INFO, logger.getLevel());
        }

        @Test
        void printPlaceholderAsIs() {
            logger.log("printing the placeholder token as-is: {}, using the arg replacement mechanism", "{}");
        }
    }

    @Nested
    class ReadmeSample {
        private final Logger defaultLogger = Logger.instance();

        @Test
        void messagesAndArgs() {
            defaultLogger.log("default logger name is usually the same as the API caller class name");
            assertEquals(ReadmeSample.class.getName(), defaultLogger.getName());
            defaultLogger.log("default log level is {}, which depends on the individual logging provider",
                    defaultLogger.getLevel());
            Logger info = defaultLogger.atInfo();
            info.log("level set omitted here but we know the level is {}", INFO);
            assertEquals(INFO, info.getLevel());
            info.log("Supplier and other Object args can be mixed: Object arg1 {}, Supplier arg2 {}, Object arg3 {}",
                    "a11111",
                    (Supplier) () -> "a22222",
                    "a33333");
            info.atWarn()
                    .log("switched to WARN level on the fly. that is, {} is a different Logger instance from {}",
                            "`info.atWarn()`",
                            "`info`");
            assertNotSame(info, info.atWarn());
            assertEquals(info.getName(), info.atWarn().getName(), "same name, only level is different");
            assertEquals(WARN, info.atWarn().getLevel());
            assertEquals(INFO, info.getLevel(), "immutable info's level never changes");
        }

        @Test
        void exceptionMessageAndArgs() {
            Logger error = defaultLogger.atError();
            Throwable ex = new Exception("ex message");
            error.log(ex);
            error.atInfo()
                    .log("{} is an immutable Logger instance whose name is {}, and level is {}",
                            error,
                            error.getName(),
                            error.getLevel());
            assertEquals(Level.ERROR, error.getLevel());
            error.atError()
                    .log(ex,
                            "here the {} call is unnecessary because a Logger instance is immutable, and the {} instance's log level is already and will always be {}",
                            "atError()",
                            error,
                            ERROR);
            error.log(ex,
                    "now at Level.ERROR, together with the exception stack trace, logging some items expensive to compute: 1. {} 2. {} 3. {} 4. {}",
                    "usually an Object-type argument's Object.toString result is used for the final log message, except that...",
                    (Supplier) () -> "the Supplier.get result will be used instead for a Supplier-type argument",
                    "this allows for a mixture of Supplier and other Object types of arguments to compute to a sensible final log message",
                    (Supplier) () -> Arrays.stream(new Object[] {
                                    "suppose this is an expensive message argument coming as a Supplier" })
                            .collect(Collectors.toList()));
        }
    }

    @Nested
    class ReadmeSample2 {
        private final Logger logger = Logger.instance(ReadmeSample2.class);

        @Test
        void levelGuard() {
            if (logger.atDebug().isEnabled()) {
                logger.atDebug()
                        .log("a {} message guarded by a {}, so that no {} is created unless this logger instance - name and level combined - is {}",
                                "long and expensive-to-construct",
                                "level check",
                                "message object",
                                "enabled by system configuration of the logging provider");
            }
            logger.atDebug()
                    .log((Supplier) () -> "alternative to the level guard, using a Supplier<?> function like this should achieve the same goal of avoiding unnecessary message creation, pending quality of the logging provider");
        }
    }

    @Nested
    class loggerName {
        @Test
        void blankOrEmptyNamesStayAsIs() {
            String blank = "   ";
            assertEquals(blank, Logger.instance(blank).getName());
            String empty = "";
            assertEquals("", Logger.instance(empty).getName());
        }

        @Test
        void optToSupplyCallerClassNameForNullOrNoargInstance() {
            Logger localLogger = Logger.instance();
            String thisClassName = this.getClass().getName();
            localLogger.log("method local logger {} in class {}", localLogger, thisClassName);

            assertSame(localLogger, Logger.instance((String) null));
            assertSame(localLogger, Logger.instance((Class<?>) null));
            assertTrue(localLogger.getName().contains(thisClassName));
        }
    }
}