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

    @Test
    void saveUser_duplicateUsername_throwsException(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        // Act
        userRepository.save(username, email, passwordHash);

        // Assert
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            userRepository.save(username, "differentemail@@example.com", "differentHash");
        }, "Should throw exception for duplicate username");
    }

    @Test
    void saveUser_duplicateEmail_throwsException(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        // Act
        userRepository.save(username, email, passwordHash);

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            userRepository.save("differentUser", email, "differentHash");
                },
                "Should throw an exception, email already exist");
    }

    @Test
        void userExistByUsername_userExist_returnsTrue(){
            // Arrange
            String username = "testuser";
            String email = "testuser@example.com";
            String passwordHash = "hashedpassword";
            userRepository.save(username, email, passwordHash);

            // Act
            assertTrue(userRepository.existsByUsername(username), "should return true, user exist by " +
                    "username");
    }

    @Test
    void userExistByUsername_userDoesNotExist_returnsFalse(){
        // Act
        assertFalse(userRepository.existsByUsername("NonexistentUser"),
                "Should return false, " +
                "username not found or exist");
    }

    @Test
    void userExistByEmail_emailExist_returnsTrue(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        userRepository.save(username, email, passwordHash);

        // Act
        assertTrue(userRepository.existsByEmail(email), "should return true, " +
                "user exist by email");
    }

    @Test
    void userExistByEmail_userDoesNotExist_returnsFalse(){
        // Act
        assertFalse(userRepository.existsByUsername("nonexistentuser@example.com"),
                "Should return false, " +
                        "user not found or exist by email");
    }

    @Test
    void findIdByUsername_userExists_returnsId(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        userRepository.save(username, email, passwordHash);

        //Act
        Long userId = userRepository.findIdByUsername(username);

        // Assert
        assertNotNull(userId, "user ID should be found by username");
        assertTrue(userId > 0, "user ID should be positive");
    }

    @Test
    void findIdByUsername_userDoesNotExists_returnsNull(){
        // Act
        Long userId = userRepository.findIdByUsername("noneExistenceUsername");

        // Assert
        assertNull(userId, "None-existence user should return null by username");
    }
}
