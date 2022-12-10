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

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static elf4j.Level.ERROR;
import static elf4j.Level.INFO;
import static org.junit.jupiter.api.Assertions.*;

class TinylogLoggerTest {

    @Nested
    class name {

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
        Logger logger = Logger.instance(placeholder.class);

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
        void cacheLoggerWithSameNameAndLevel() {
            Logger error = logger.atError();
            Logger error2 = logger.atError();
            assertSame(error, error2, "logger instance of the same name and level is cached and re-used");
        }

        @Test
        void printPlaceholderAsIs() {
            logger.log("printing the placeholder token as-is: {}, using the arg replacement mechanism", "{}");
        }
    }

    @Nested
    class ReadmeSample {
        private final Logger logger = Logger.instance(ReadmeSample.class);

        @Test
        void messagesArgsAndGuards() {
            logger.log("logger name {} is the same as param class name {}",
                    logger.getName(),
                    ReadmeSample.class.getName());
            assertEquals(ReadmeSample.class.getName(), logger.getName());
            logger.log("default log level is {}, which depends on the individual logging provider", logger.getLevel());
            Logger info = logger.atInfo();
            info.log("level set omitted here but we know the level is {}", INFO);
            assertEquals(INFO, info.getLevel());
            info.log("Supplier and Object args can be mixed: Object arg1 {}, Supplier arg2 {}, Object arg3 {}",
                    "a11111",
                    (Supplier) () -> "a22222",
                    "a33333");
            info.atWarn()
                    .log("switched to WARN level on the fly. that is, {} is a different Logger instance from {}",
                            "`info.atWarn()`",
                            "`info`");
            assertNotSame(info, info.atWarn());
            assertEquals(INFO, info.getLevel(), "immutable info's level/state never changes");

            Logger debug = logger.atDebug();
            assertNotSame(logger, debug, "different instances of different levels");
            assertEquals(logger.getName(), debug.getName(), "same name, only level is different");
            assertEquals(Level.DEBUG, debug.getLevel());
            if (debug.isEnabled()) {
                debug.log(
                        "a {} message guarded by a {}, so that no {} is created unless this logger instance - name and level combined - is {}",
                        "long and expensive-to-construct",
                        "level check",
                        "message object",
                        "enabled by system configuration of the logging provider");
            }
            debug.log((Supplier) () -> "alternative to the level guard, using a Supplier<?> function like this should achieve the same goal of avoiding unnecessary message creation, pending quality of the logging provider");
        }
    }

    @Nested
    class ReadmeSample2 {
        private final Logger error = Logger.instance(ReadmeSample2.class).atError();

        @Test
        void throwableAndMessageAndArgs() {
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
                    "now at Level.ERROR, together with the exception stack trace, logging some items expensive to compute: item1 {}, item2 {}, item3 {}, item4 {}, ...",
                    "i11111",
                    (Supplier) () -> "i22222",
                    "i33333",
                    (Supplier) () -> Arrays.stream(new Object[] { "i44444" }).collect(Collectors.toList()));
        }
    }
}