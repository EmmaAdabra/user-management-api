package com.adb.usermanagementapi.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LoginAttemptsRepositoryImpl implements LoginAttemptsRepository{
    private final JdbcTemplate jdbcTemplate;
    private static final String COUNT_USER_FAILED_LOGIN_WITHIN_GIVEN_TIME =  "SELECT COUNT(*) " +
            "FROM login_attempts WHERE user_id = ? AND success = ? AND attempt_time >= ?";
    private static final String INSERT_INTO_LOGIN_ATTEMPTS_BY_ID = "INSERT INTO login_attempts " +
            "(user_id, attempt_time, success) VALUES (?, ?, ?)";
    private static final String SELECT_LAST_FAILED_ATTEMPTS_TIME = "SELECT MAX(attempt_time) FROM login_attempts WHERE user_id = ? AND success " +
            "= false";

    public LoginAttemptsRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int countFailedAttemptsInLastTwoMinutes(Long userId) {
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        return jdbcTemplate.queryForObject(COUNT_USER_FAILED_LOGIN_WITHIN_GIVEN_TIME, Integer.class, userId, false, twoMinutesAgo);
    }

    @Override
    public void logLoginAttempt(Long userId, boolean success) {
        jdbcTemplate.update(INSERT_INTO_LOGIN_ATTEMPTS_BY_ID, userId, LocalDateTime.now(), success);
    }

    @Override
    public Timestamp lastFailedLoginAttempt(Long user_id) {
        try {
            return jdbcTemplate.queryForObject(SELECT_LAST_FAILED_ATTEMPTS_TIME, Timestamp.class, user_id);
        } catch (EmptyResultDataAccessException e){
            return null;
        }
    }
}
