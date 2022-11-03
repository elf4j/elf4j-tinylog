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
import java.util.stream.Collectors;

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
        Logger logger = Logger.instance();

        @Test
        void defaultLevelAndCacheAndPlaceholderPrint() {
            logger.log("logger instance {}'s default level is {}", logger, logger.getLevel());
            assertEquals(INFO, logger.getLevel());
            Logger info = logger.atInfo();
            assertSame(logger, info, "logger instance of the same name and level is cached and re-used");
            info.log("printing the placeholder token as-is: {}, using the arg replacement mechanism", "{}");
        }
    }

    @Nested
    class readmeSamples {
        private final Logger logger = Logger.instance(readmeSamples.class).atInfo();

        @Test
        void messagesArgsAndGuards() {
            assertEquals(INFO, logger.getLevel());
            logger.log("info level message with arguments - arg1 {}, arg2 {}, arg3 {}", "a11111", "a22222", "a33333");
            logger.atWarn().log("switched to warn level on the fly");
            assertEquals(INFO, logger.getLevel(), "immutable logger's level/state never changes");

            Logger debug = logger.atDebug();
            assertNotSame(logger, debug, "different instances of different levels");
            assertEquals(logger.getName(), debug.getName(), "same name, only level is different");
            assertEquals(Level.DEBUG, debug.getLevel());
            if (debug.isEnabled()) {
                debug.log("a {} message guarded by a {}, so that no {} is created unless DEBUG level is {}",
                        "long and expensive-to-construct",
                        "level check",
                        "message object",
                        "enabled by system configuration of the logging provider");
            }
            debug.log(() -> "alternative to the level guard, using a Supplier<?> function like this should achieve the same goal of avoiding unnecessary message creation, pending quality of the logging provider");
        }

        @Test
        void throwableAndMessageAndArgs() {
            Logger error = logger.atError();
            error.log("this is an immutable Logger instance whose level is Level.ERROR");
            Throwable ex = new Exception("ex message");
            assertEquals(Level.ERROR, error.getLevel());
            error.log(ex, "level set omitted but we know the level is Level.ERROR");
            error.atWarn()
                    .log(ex,
                            "switched to warn level on the fly. that is, {} returns a {} and {} Logger {}",
                            "atWarn()",
                            "different",
                            "immutable",
                            "instance");
            error.atError()
                    .log(ex,
                            "here the {} call is {} because a Logger instance is {}, and the instance's log level has and will always be Level.ERROR",
                            "atError()",
                            "unnecessary",
                            "immutable");
            error.log(ex,
                    "now at Level.ERROR, together with the exception stack trace, logging some items expensive to compute: item1 {}, item2 {}, item3 {}, item4 {}, ...",
                    () -> "i11111",
                    () -> "i22222",
                    () -> "i33333",
                    () -> Arrays.stream(new Object[] { "i44444" }).collect(Collectors.toList()));
        }
    }
}