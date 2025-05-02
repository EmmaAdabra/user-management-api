package com.adb.usermanagementapi.repository;

public interface loginAttemptsRepository {
    int countFailedAttemptsInLastTwoMinutes(Long userId);
    public void logLoginAttempt(Long userId, boolean success);
}
