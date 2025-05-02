package com.adb.usermanagementapi.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

public class LoginAttemptsRepositoryImpl implements LoginAttemptsRepository{
    private final JdbcTemplate jdbcTemplate;

    public LoginAttemptsRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int countFailedAttemptsInLastTwoMinutes(Long userId) {
        String sql = "SELECT COUNT(*) FROM login_attempts WHERE user_id = ? AND success = ? AND " +
                "attempt_time >= ?";
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, false, twoMinutesAgo);
    }

    @Override
    public void logLoginAttempt(Long userId, boolean success) {
        String sql = "INSERT INTO login_attempts (user_id, attempt_time, success) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, LocalDateTime.now(), success);
    }
}
