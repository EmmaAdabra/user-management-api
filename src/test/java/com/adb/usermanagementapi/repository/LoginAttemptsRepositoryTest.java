package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.config.TestUserRepositoryConfig;
import com.adb.usermanagementapi.model.User;
import com.adb.usermanagementapi.model.dto.LoginAttempt;
import com.adb.usermanagementapi.repository.LoginAttemptsRepository;
import com.adb.usermanagementapi.repository.UserRepository;
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

@SpringJUnitConfig(TestUserRepositoryConfig.class)
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
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        User testUser = userRepository.save(TestUtils.getUser(username, email, passwordHash));

        int interOfFiveMinutes = 5;

        // Act
        loginAttemptsRepository.logLoginAttempt(testUser.getId(), false);
        loginAttemptsRepository.logLoginAttempt(testUser.getId(), false);
        loginAttemptsRepository.logLoginAttempt(testUser.getId(), true);

        List<LoginAttempt> loginAttemptList = loginAttemptsRepository.findRecentLogins(testUser.getId(),
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

        User testUser = userRepository.save(TestUtils.getUser(username, email, passwordHash));


        Timestamp failedTime1 = Timestamp.valueOf("2025-04-01 08:00:00");
        Timestamp failedTime2 = Timestamp.valueOf("2025-04-02 09:30:00");
        Timestamp successTime = Timestamp.valueOf("2025-04-03 11:00:00");

        insertLoginAttempt(testUser.getId(), failedTime1, false);
        insertLoginAttempt(testUser.getId(), failedTime2, false);
        insertLoginAttempt(testUser.getId(), successTime, true);

        Timestamp result = loginAttemptsRepository.lastFailedLoginAttempt(testUser.getId());

        assertEquals(failedTime2, result);
    }

    @Test
    void lastFailedLoginAttempt_shouldReturnNull_whenNoFailuresExist() {
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        User testUser = userRepository.save(TestUtils.getUser(username, email, passwordHash));


        Timestamp successTime = Timestamp.valueOf("2023-04-03 11:00:00");

        insertLoginAttempt(testUser.getId(), successTime,true);

        Timestamp result = loginAttemptsRepository.lastFailedLoginAttempt(testUser.getId());

        assertNull(result);
    }



    @Test
    void findRecentLogins_existingLoginsWithinSpecificInterval_returnsNonEmptyList(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        User testUser = userRepository.save(TestUtils.getUser(username, email, passwordHash));

        insertLoginAttempt(testUser.getId(), Timestamp.valueOf(LocalDateTime.now().minusMinutes(30)),
                false);
        insertLoginAttempt(testUser.getId(), Timestamp.valueOf(LocalDateTime.now()), false);
        insertLoginAttempt(testUser.getId(), Timestamp.valueOf(LocalDateTime.now()), true);

        // get all login attempt within the last 10 minutes
        List<LoginAttempt> loginAttemptList = loginAttemptsRepository.findRecentLogins(
                testUser.getId(), 10);

        assertEquals(2, loginAttemptList.size(), "There should be 2 login attempt within 10 " +
                "minutes for the user ID");
        assertSame(loginAttemptList.get(0).userId(), testUser.getId());
    }

    @Test
    void findRecentLogins_noExistingLoginsWithinSpecificInterval_returnsEmptyList(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        User testUser = userRepository.save(TestUtils.getUser(username, email, passwordHash));

        insertLoginAttempt(testUser.getId(), Timestamp.valueOf(LocalDateTime.now().minusMinutes(30)), false);
        insertLoginAttempt(testUser.getId(), Timestamp.valueOf(LocalDateTime.now().minusMinutes(20)), false);
        insertLoginAttempt(testUser.getId(), Timestamp.valueOf(LocalDateTime.now().minusMinutes(15)), true);

        // get all login attempt within the last 10 minutes
        List<LoginAttempt> loginAttemptList = loginAttemptsRepository.findRecentLogins(
                testUser.getId(), 10);

        assertTrue(loginAttemptList.isEmpty(), "Should be no existing login attempts within " +
                "the last 10 minutes for provided user ID");
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
