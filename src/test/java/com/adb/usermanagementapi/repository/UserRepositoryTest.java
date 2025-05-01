package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

// load beans form the application isolated test context for the tests
@SpringJUnitConfig(TestConfig.class)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    // run before each test, to ensure clean database state
    @BeforeEach
    void setUp(){
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM login_attempts");
    }

    @Test
    void saveUser_success(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        // Act
        userRepository.save(username, email, passwordHash);

        // Assert
        assertTrue(userRepository.existsByUsername(username), "User with the - " + username + " " +
                "should be found, after saving");
        assertTrue(userRepository.existsByEmail(email), "User with the - " + email + " should " +
                "be found, after saving");

        // Act
        Long userIdByUsername = userRepository.findIdByUsername(username);
        Long userIdByEmail = userRepository.findIdByEmail(email);

        // Assert
        assertNotNull(userIdByUsername, "User ID should be found by username");
        assertNotNull(userIdByEmail, "User ID should be found by email");
        assertEquals(userIdByUsername, userIdByEmail, "User ID should match");
    }
}
