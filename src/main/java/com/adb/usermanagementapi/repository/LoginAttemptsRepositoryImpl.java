package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.model.dto.LoginAttempt;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class LoginAttemptsRepositoryImpl implements LoginAttemptsRepository{
    private final JdbcTemplate jdbcTemplate;
    private static final String SELECT_RECENT_LOGIN_WITHIN_GIVEN_TIME =  "SELECT id, user_id, " +
            "attempt_time, success FROM login_attempts WHERE user_id = ? AND attempt_time >= ? " +
            "LIMIT 20";
    private static final String INSERT_INTO_LOGIN_ATTEMPTS_BY_ID = "INSERT INTO login_attempts " +
            "(user_id, attempt_time, success) VALUES (?, ?, ?);";
    private static final String SELECT_LAST_FAILED_ATTEMPTS_TIME = "SELECT MAX(attempt_time) FROM login_attempts WHERE user_id = ? AND success " +
            "= false";

    private static final RowMapper<LoginAttempt> LOGIN_ATTEMPT_ROW_MAPPER = (rs, noRow) -> {
        return  new LoginAttempt(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getTimestamp("attempt_time").toLocalDateTime(),
                rs.getBoolean("success")
        );
    };

    public LoginAttemptsRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

    @Override
    public List<LoginAttempt> findRecentLogins(Long userId, int timeInterval) {
        LocalDateTime interval = LocalDateTime.now().minusSeconds(timeInterval);
        try{
            return jdbcTemplate.query(SELECT_RECENT_LOGIN_WITHIN_GIVEN_TIME,
                    LOGIN_ATTEMPT_ROW_MAPPER, userId, interval);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            return  Collections.emptyList();
        }
    }

    @Override
    public int countFailedAttemptsInLastTwoMinutes(Long userId) {
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        return jdbcTemplate.queryForObject(SELECT_RECENT_LOGIN_WITHIN_GIVEN_TIME, Integer.class, userId, false, twoMinutesAgo);
    }
}
