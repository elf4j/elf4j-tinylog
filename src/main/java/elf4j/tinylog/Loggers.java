package elf4j.tinylog;

import elf4j.Logger;

public class Loggers {
    private Loggers() {
    }

    public static void renew() {
        ((TinylogLogger) Logger.instance()).getTinylogLoggerFactory().evictCachedLoggers();
    }

    public static void renew(String loggerNameStartPattern) {
        ((TinylogLogger) Logger.instance()).getTinylogLoggerFactory().evictCachedLoggers(loggerNameStartPattern);
    }
}
