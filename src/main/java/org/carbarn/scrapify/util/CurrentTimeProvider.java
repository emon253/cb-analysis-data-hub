package org.carbarn.scrapify.util;

import java.time.LocalDateTime;

public class CurrentTimeProvider {
    // Private constructor to prevent instantiation from outside
    private CurrentTimeProvider() {
    }

    // Singleton instance
    private static final CurrentTimeProvider INSTANCE = new CurrentTimeProvider();

    // Get the singleton instance
    public static CurrentTimeProvider getInstance() {
        return INSTANCE;
    }

    // Get the current time
    public LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }
}
