package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.model.login.LoginAttempt;

import java.sql.Timestamp;
import java.util.List;

public interface LoginAttemptsRepository {
    public void logLoginAttempt(Long userId, boolean success);
    public Timestamp lastFailedLoginAttempt(Long user_id);
    List<LoginAttempt> findRecentLogins(Long userId, int timeInterval);
}
