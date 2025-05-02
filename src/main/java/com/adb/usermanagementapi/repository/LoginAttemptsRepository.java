package com.adb.usermanagementapi.repository;

public interface LoginAttemptsRepository {
    int countFailedAttemptsInLastTwoMinutes(Long userId);
    public void logLoginAttempt(Long userId, boolean success);
}
