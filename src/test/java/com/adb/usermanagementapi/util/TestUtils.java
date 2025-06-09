package com.adb.usermanagementapi.util;

import com.adb.usermanagementapi.model.User;

import java.time.LocalDateTime;

public class TestUtils {
    public static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Respect thread interruption
        }
    }

    public static User getUser(String username, String email, String passwordHash){
        return new User(null, username, email, passwordHash, LocalDateTime.now(), false);
    }

    public static User getUser(String email, String passwordHash, boolean lockStatus){
        return new User(1L, "username", email, passwordHash, LocalDateTime.now(), lockStatus);
    }
}
