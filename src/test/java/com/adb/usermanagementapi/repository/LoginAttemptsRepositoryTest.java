package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.config.TestConfig;
import com.adb.usermanagementapi.model.dto.LoginAttempt;
import com.adb.usermanagementapi.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(TestConfig.class)
public class LoginAttemptsRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private LoginAttemptsRepository loginAttemptsRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        jdbcTemplate.update("DELETE FROM login_attempts");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void countFailedAttemptsInLastTwoMinutes_noAttempts_returnsZero(){
        String username = "testuser";
        userRepository.save(username, "testuser@example.com", "hashedpassword");
        Long userId = userRepository.findIdByUsername(username);

        int count = loginAttemptsRepository.countFailedAttemptsInLastTwoMinutes(userId);
        assertEquals(0, count, "No login attempts should return zero");
    }

    @Test
    void countFailedAttemptsInLastTwoMinutes_multipleFailedAttempts_returnsCorrectCount() {
        // Arrange
        String username = "testuser";
        userRepository.save(username, "testuser@example.com", "hashedpassword");
        Long userId = userRepository.findIdByUsername(username);
        loginAttemptsRepository.logLoginAttempt(userId, false);
        loginAttemptsRepository.logLoginAttempt(userId, true);
        loginAttemptsRepository.logLoginAttempt(userId, false);

        // Act
        int count = loginAttemptsRepository.countFailedAttemptsInLastTwoMinutes(userId);

        // Assert
        assertEquals(2, count, "Should count only failed attempts within 2 minutes");
    }

    @Test
    void logLoginAttempt_failedAttempt_logsCorrectly(){
        // Arrange
        String username = "testuser";
        userRepository.save(username, "testuser@example.com", "hashedpassword");
        Long userId = userRepository.findIdByUsername(username);

        // Act
        loginAttemptsRepository.logLoginAttempt(userId, false);
        loginAttemptsRepository.logLoginAttempt(userId, false);

        String detailSql = "SELECT success, user_id FROM login_attempts WHERE user_id = ? " +
                "ORDER BY  attempt_time DESC LIMIT 1";
        Object[] result = jdbcTemplate.queryForObject(detailSql, (rs, rowNum) -> new Object[]{
                rs.getLong("user_id"),
                rs.getBoolean("success")
        }, userId);

        assertEquals(userId, result[0], "user ID should match");
        assertFalse((Boolean) result[1], "Success should be false");
    }

    @Test
    void logLoginAttempt_successfulAttempt_logsCorrectly(){
        // Arrange
        String username = "testuser";
        userRepository.save(username, "testuser@example.com", "hashedpassword");
        Long userId = userRepository.findIdByUsername(username);

        // Act
        loginAttemptsRepository.logLoginAttempt(userId, true);
        loginAttemptsRepository.logLoginAttempt(userId, false);
        loginAttemptsRepository.logLoginAttempt(userId, true);

        // Assert: Check count of failed login attempts
        String countSql = "SELECT COUNT(*) FROM login_attempts WHERE user_id = ? AND success = true";
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
        assertEquals(2, count, "Two successful attempt should be logged");

        // Additional test
        String detailSql = "SELECT success, user_id FROM login_attempts WHERE user_id = ? " +
                "ORDER BY  attempt_time DESC";
        Object[] result = jdbcTemplate.queryForObject(detailSql, (rs, rowNum) -> new Object[]{
                rs.getLong("user_id"),
                rs.getBoolean("success")
        }, userId);

        assertEquals(userId, result[0], "user ID should match");
        assertTrue((Boolean) result[1], "Success should be true");
    }

    @Test
    void lastFailedLoginAttempt_shouldReturnLatestFailedAttempt_whenFailuresExist() {
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        Timestamp createdAt = Timestamp.valueOf("2025-02-05 10:00:00");
        Timestamp failedTime1 = Timestamp.valueOf("2025-04-01 08:00:00");
        Timestamp failedTime2 = Timestamp.valueOf("2025-04-02 09:30:00");
        Timestamp successTime = Timestamp.valueOf("2025-04-03 11:00:00");

        userRepository.save(username, email, passwordHash);

        Long userId = userRepository.findIdByUsername(username);
        insertLoginAttempt(userId, failedTime1, false);
        insertLoginAttempt(userId, failedTime2, false);
        insertLoginAttempt(userId, successTime, true);

        Timestamp result = loginAttemptsRepository.lastFailedLoginAttempt(userId);

        assertEquals(failedTime2, result);
    }

    @Test
    void lastFailedLoginAttempt_shouldReturnNull_whenNoFailuresExist() {
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        Timestamp createdAt = Timestamp.valueOf("2023-02-01 12:00:00");
        Timestamp successTime = Timestamp.valueOf("2023-04-03 11:00:00");

        userRepository.save(username, email, passwordHash);
        Long userId = userRepository.findIdByUsername(username);
        insertLoginAttempt(userId, successTime,true);

        Timestamp result = loginAttemptsRepository.lastFailedLoginAttempt(userId);

        assertNull(result);
    }

    private void insertLoginAttempt(Long userId, Timestamp attemptTime, boolean success) {
        jdbcTemplate.update(
                "INSERT INTO login_attempts (user_id, attempt_time, success) VALUES (?, ?, ?)",
                userId, attemptTime, success
        );
    }

    @Test
    void findRecentLogins_existingLoginsWithinSpecificInterval_returnsNonEmptyList(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        userRepository.save(username, email, passwordHash);

        Long userId = userRepository.findIdByUsername(username);

        loginAttemptsRepository.logLoginAttempt(userId, false);
        TestUtils.delay(3000); // delay for 3 seconds
        loginAttemptsRepository.logLoginAttempt(userId, true);
        loginAttemptsRepository.logLoginAttempt(userId, false);

        // get all login attempt within the last 2 seconds
        List<LoginAttempt> loginAttemptList = loginAttemptsRepository.findRecentLogins(userId, 2);

        assertEquals(2, loginAttemptList.size(), "There should be 2 login attempt within 2 " +
                "seconds for the user ID");
        assertSame(loginAttemptList.get(0).userId(), userId);
    }

    @Test
    void findRecentLogins_noExistingLoginsWithinSpecificInterval_returnsEmptyList(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        userRepository.save(username, email, passwordHash);

        Long userId = userRepository.findIdByUsername(username);

        loginAttemptsRepository.logLoginAttempt(userId, false);
        loginAttemptsRepository.logLoginAttempt(userId, true);
        loginAttemptsRepository.logLoginAttempt(userId, false);
        TestUtils.delay(3000); // delay for 3 seconds

        // get all login attempt within the last 2 seconds
        List<LoginAttempt> loginAttemptList = loginAttemptsRepository.findRecentLogins(userId, 2);

        assertTrue(loginAttemptList.isEmpty(), "Should be no existing login attempts within 2 " +
                "seconds for" +
                " provided user ID");
    }

    @Test
    void findRecentLogins_noneExistingUser_returnsEmptyList(){
        // Arrange
        Long noneExistingUserId = 900L;

        // Act
        List<LoginAttempt> loginAttemptList = loginAttemptsRepository.findRecentLogins(noneExistingUserId, 2);

        // Assert
        assertTrue(loginAttemptList.isEmpty(), "Should be no login attempts for none-existing " +
                "user");
    }
}
