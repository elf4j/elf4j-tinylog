package elf4j.tinylog;

public class Loggers {
    private Loggers() {
    }

    public static void renew() {
        TinylogLogger.evictCachedLoggers();
    }

    public static void renew(String loggerNameStartPattern) {
        TinylogLogger.evictCachedLoggers(loggerNameStartPattern);
    }
}
