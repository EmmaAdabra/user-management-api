package com.adb.usermanagementapi.util;

public class TestUtils {
    public static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Respect thread interruption
        }
    }
}
