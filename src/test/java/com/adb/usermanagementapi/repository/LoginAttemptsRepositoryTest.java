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

    // helper method
    private void insertLoginAttempt(Long userId, Timestamp attemptTime, boolean success) {
        jdbcTemplate.update(
                "INSERT INTO login_attempts (user_id, attempt_time, success) VALUES (?, ?, ?)",
                userId, attemptTime, success
        );
    }

    @Test
    void logLoginAttempt_successful_logsCorrectly(){
        // Arrange
        String username = "testuser";
        userRepository.save(username, "testuser@example.com", "hashedpassword");
        Long userId = userRepository.findIdByUsername(username);
        int interOfFiveMinutes = 5;

        // Act
        loginAttemptsRepository.logLoginAttempt(userId, false);
        loginAttemptsRepository.logLoginAttempt(userId, false);
        loginAttemptsRepository.logLoginAttempt(userId, true);

        List<LoginAttempt> loginAttemptList = loginAttemptsRepository.findRecentLogins(userId,
                interOfFiveMinutes);

        // Assert
        assertEquals(3, loginAttemptList.size(), "Login attempts for user the ID within 5 minutes" +
                " should be 3");
        assertFalse(loginAttemptList.get(0).success(), "login attempt status for the user Id " +
                "should be false");
    }

    @Test
    void lastFailedLoginAttempt_shouldReturnLatestFailedAttempt_whenFailuresExist() {
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

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

        Timestamp successTime = Timestamp.valueOf("2023-04-03 11:00:00");

        userRepository.save(username, email, passwordHash);
        Long userId = userRepository.findIdByUsername(username);
        insertLoginAttempt(userId, successTime,true);

        Timestamp result = loginAttemptsRepository.lastFailedLoginAttempt(userId);

        assertNull(result);
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
