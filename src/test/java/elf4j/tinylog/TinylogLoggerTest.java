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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TinylogLoggerTest {

    @Nested
    class levels {
        private final Logger LOGGER = Logger.instance(TinylogLoggerTest.class);

        @Test
        void optToSupplyDefaultLevelAsInfo() {
            assertEquals(Level.INFO, LOGGER.getLevel());
            LOGGER.log("opt to provide default level");
        }

        @ParameterizedTest
        @ValueSource(strings = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF" })
        void atHonorsCustomLevelArgs(String level) {
            assertEquals(Level.valueOf(level), LOGGER.atLevel(Level.valueOf(level)).getLevel());
        }

        @Test
        void noArgAtHonorsLeveOnMethodName() {
            assertEquals(Level.TRACE, LOGGER.atTrace().getLevel());
            assertEquals(Level.DEBUG, LOGGER.atDebug().getLevel());
            assertEquals(Level.INFO, LOGGER.atInfo().getLevel());
            assertEquals(Level.WARN, LOGGER.atWarn().getLevel());
            assertEquals(Level.ERROR, LOGGER.atError().getLevel());
        }
    }

    @Nested
    class log {
        public final Logger LOGGER = Logger.instance(TinylogLoggerTest.class);

        @Test
        void object() {
            LOGGER.log("log message");
        }

        @Test
        void supplier() {
            LOGGER.atLevel(Level.TRACE).log(() -> "supplier message");
        }

        @Test
        void messageAndArgs() {
            LOGGER.atLevel(Level.INFO).log("{} is a shorthand of {}", "atInfo()", "atLevel(Level.INFO)");
        }

        @Test
        void messageAndSuppliers() {
            LOGGER.atLevel(Level.WARN)
                    .log("message supplier arg1 {}, arg2 {}, arg3 {}",
                            () -> "a11111",
                            () -> "a22222",
                            () -> Arrays.asList("a33333"));
        }

        @Test
        void throwable() {
            LOGGER.atLevel(Level.ERROR).log(new Exception("ex message"));
        }

        @Test
        void throwableAndMessage() {
            LOGGER.atLevel(Level.ERROR).log(new Exception("ex message"), "log message");
        }

        @Test
        void throwableAndSupplier() {
            LOGGER.atLevel(Level.ERROR).log(new Exception("ex message"), () -> "supplier log message");
        }

        @Test
        void throwableAndMessageAndArgs() {
            LOGGER.atLevel(Level.ERROR).log(new Exception("ex message"), "log message with arg {}", "a11111");
        }

        @Test
        void throwableAndMessageAndSupplierArgs() {
            LOGGER.atError()
                    .log(new Exception("ex message"),
                            "log message with supplier arg1 {}, arg2 {}, arg3 {}",
                            () -> "a11111",
                            () -> "a22222",
                            () -> Arrays.stream(new Object[] { "a33333" }).collect(Collectors.toList()));
        }
    }

    @Nested
    class name {
        private final Logger LOGGER = Logger.instance(TinylogLoggerTest.class);

        @Test
        void optToSupplyCallerClassNameForNullOrNoargInstance() {
            String thisClassName = this.getClass().getName();
            assertTrue(thisClassName.contains(Logger.instance().getName()));
            assertTrue(thisClassName.contains(Logger.instance((String) null).getName()));
            assertTrue(thisClassName.contains(Logger.instance((Class<?>) null).getName()));
        }

        @Test
        void blankOrEmptyNamesStayAsIs() {
            String blank = "   ";
            assertEquals(blank, Logger.instance(blank).getName());
            String empty = "";
            assertEquals("", Logger.instance(empty).getName());
        }
    }

    @Nested
    class placeholder {
        Logger logger = Logger.instance(placeholder.class).atInfo();

        @Test
        void simulateTokenEscape() {
            logger.log("printing the placeholder token as-is - {} - using {}", "{}", "the arg replacement mechanism");
        }
    }

    @Nested
    class readmeSamples {
        private final Logger logger = Logger.instance(readmeSamples.class);

        @Test
        void messageAndArgs() {
            logger.atInfo().log("info message");
            logger.atLevel(Level.INFO).log("{} is a shorthand of {}", "atInfo()", "atLevel(Level.INFO)");
            logger.atWarn()
                    .log("warn message with supplier arg1 {}, arg2 {}, arg3 {}",
                            () -> "a11111",
                            () -> "a22222",
                            () -> Arrays.stream(new Object[] { "a33333" }).collect(Collectors.toList()));
        }

        @Test
        void throwableAndMessageAndArgs() {
            logger.atInfo().log("let see immutability in action...");
            Logger errorLogger = logger.atError();
            Throwable ex = new Exception("ex message");
            errorLogger.log(ex, "level set omitted, the log level is Level.ERROR");
            errorLogger.atWarn()
                    .log(ex,
                            "the log level switched to WARN on the fly. that is, {} returns a {} and {} Logger {}",
                            "atWarn()",
                            "different",
                            "immutable",
                            "instance");
            errorLogger.atError()
                    .log(ex,
                            "the atError() call is {} because the errorLogger instance is {}, and the instance's log level has always been Level.ERROR",
                            "unnecessary",
                            "immutable");
            errorLogger.log(ex,
                    "now at Level.ERROR, together with the exception stack trace, logging some items expensive to compute: item1 {}, item2 {}, item3 {}, item4 {}, ...",
                    () -> "i11111",
                    () -> "i22222",
                    () -> Arrays.asList("i33333"),
                    () -> Arrays.stream(new Object[] { "i44444" }).collect(Collectors.toList()));
        }
    }
}