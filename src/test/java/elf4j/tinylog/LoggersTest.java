package elf4j.tinylog;

import elf4j.Logger;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggersTest {
    @Nested
    class reset {
        @Test
        void refreshLoggerInstancesWhenReset() {
            final String sameLoggerName = "testLoggerName";
            final Logger logger1 = Logger.instance(sameLoggerName);
            final Logger logger2 = Logger.instance(sameLoggerName);
            assertTrue(logger1 instanceof TinylogLogger);
            assertTrue(logger2 instanceof TinylogLogger);
            assertSame(logger1, logger2);

            Loggers.renew();
            final Logger logger3 = Logger.instance(sameLoggerName);

            assertNotNull(logger1);
            assertSame(logger1, logger2);
            assertNotSame(logger1, logger3);
        }
    }

    @Nested
    class resetNamed {
        @Test
        void refreshLoggerInstancesWhenReset() {
            final String sameLoggerName = "testLoggerName";
            final Logger logger1 = Logger.instance(sameLoggerName);
            final Logger logger2 = Logger.instance(sameLoggerName);
            assertTrue(logger1 instanceof TinylogLogger);
            assertTrue(logger2 instanceof TinylogLogger);
            assertSame(logger1, logger2);

            Loggers.renew(sameLoggerName);
            final Logger logger3 = Logger.instance(sameLoggerName);

            assertNotNull(logger1);
            assertSame(logger1, logger2);
            assertNotSame(logger1, logger3);
        }
    }
}