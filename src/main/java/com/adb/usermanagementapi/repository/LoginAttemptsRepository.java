package com.adb.usermanagementapi.repository;

import java.sql.Timestamp;

public interface LoginAttemptsRepository {
    int countFailedAttemptsInLastTwoMinutes(Long userId);
    public void logLoginAttempt(Long userId, boolean success);
    public Timestamp lastFailedLoginAttempt(Long user_id);
}
