package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.config.TestConfig;
import com.adb.usermanagementapi.exception.UserNotFoundException;
import com.adb.usermanagementapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

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
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () ->
            userRepository.save(username, "differentemail@@example.com", "differentHash"), "Should throw exception for duplicate username");
    }

    @Test
    void saveUser_duplicateEmail_throwsException(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        // Act
        userRepository.save(username, email, passwordHash);

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () ->
            userRepository.save("differentUser", email, "differentHash"),
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

    @Test
    void findIdByEmail_userExists_returnsId(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        userRepository.save(username, email, passwordHash);

        //Act
        Long userId = userRepository.findIdByEmail(email);

        // Assert
        assertNotNull(userId, "user ID should be found by email");
        assertTrue(userId > 0, "user ID should be positive");
    }

    @Test
    void findIdByEmail_userDoesNotExists_returnsNull(){
        // Act
        Long userId = userRepository.findIdByUsername("nonexistentuser@example.com");

        // Assert
        assertNull(userId, "None-existence user should return null by email");
    }

    @Test
    void findByUsername_UserExists_returnsUser(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        userRepository.save(username, email, passwordHash);

        // Act
        User user = userRepository.findByUsername(username);

        assertNotNull(user, "User should be found");
        assertEquals(username, user.getUsername(), "username should match");
        assertEquals(email, user.getEmail(), "email should match");
        assertEquals(passwordHash, user.getPasswordHash(), "passwordHash should match");
        assertNotNull(user.getCreatedAt(), "Created at should be set");
    }

    @Test
    void findByUsername_userDoesNotExist_returnsNull() {
        User user = userRepository.findByUsername("nonexistent");
        assertNull(user, "Non-existent user should return null");
    }

    @Test
    void findAll_noUser_returnEmptyList(){
        List<User> users = userRepository.findAll();
        assertTrue(users.isEmpty(), "Should return empty list when no users exist");
    }

    @Test
    void findAll_multipleUsers_returnsAllUsers() {
        // Arrange
        userRepository.save("user1", "user1@example.com", "hash1");
        userRepository.save("user2", "user2@example.com", "hash2");

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertEquals(2, users.size(), "users be total of two users");

        // Arrange
        User user1 =
                users.stream().filter(u -> u.getUsername().equals("user1")).findFirst().orElse(null);
        User user2 =
                users.stream().filter(u -> u.getUsername().equals("user2")).findFirst().orElse(null);

        assertNotNull(user1, "user1 should be found");
        assertNotNull(user2, "user1 should be found");
        assertEquals("user1@example.com", user1.getEmail(), "user1 email should match");
        assertEquals("user2@example.com", user2.getEmail(), "user2 email should match");
    }

    @Test
    void updateUser_success() {
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        userRepository.save(username, email, passwordHash);

        String updatedUserName = "newuser";
        String updatedUserEmail = "newuser@gmail.com";

        Long existingUserId = userRepository.findIdByUsername(username);

        userRepository.updateUser(updatedUserName, updatedUserEmail, existingUserId);

        User foundUser = userRepository.findByUsername(updatedUserName);
        assertNull(userRepository.findByUsername(username), "Old username should no longer be found");
        assertNotNull(foundUser, "Updated user should be found");
        assertEquals(updatedUserName, foundUser.getUsername(), "Username should be updated");
        assertEquals(updatedUserEmail, foundUser.getEmail(), "Email should be updated");
        assertEquals(passwordHash, foundUser.getPasswordHash(), "Password hash should remain unchanged");
    }

    @Test
    void updateUser_userNotFound_throwsException(){
        Long noneExistenceUserId = 90L;

        assertThrows(UserNotFoundException.class, () -> userRepository.updateUser(
                "noneexistenceuser",
                "noneExistenceUser@gmail.com", noneExistenceUserId), "Should throw " +
                "exception for non-existent user");
    }

    @Test
    void updatePassword_success(){
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        userRepository.save(username, email, passwordHash);

        String updatedPassword = "newHashPassword";
        userRepository.updatePassword(username, updatedPassword);

        User user = userRepository.findByUsername(username);
        assertNotNull(user, "user should be found");
        assertEquals(updatedPassword, user.getPasswordHash(), "updatedPassword should match use " +
                "passwordhash");
    }

    @Test
    void updatePassword_userNotFound_throwsException() {
        assertThrows(UserNotFoundException.class, () ->
            userRepository.updatePassword("nonexistent", "newhashedpassword"), "Should throw exception for non-existent user");
    }

    @Test
    void deleteByUsername_success() {
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        userRepository.save(username, email, passwordHash);

        userRepository.deleteByUsername(username);

        assertFalse(userRepository.existsByUsername(username), "User should be deleted");
        assertFalse(userRepository.existsByEmail(email), "User email should be deleted");
    }

    @Test
    void deleteByUsername_userNotFound_throwsException() {
        assertThrows(UserNotFoundException.class, () -> userRepository.deleteByUsername("nonexistent"),
                "Should throw exception for non-existent user");
    }

    @Test
    void isUserLocked_userIsLocked_returnTrue(){
        // Arrange
        String username = "lockeduser";
        String email = "lockeduser@example.com";
        String passwordHash = "hashedpassword";

        userRepository.save(username, email, passwordHash);
        userRepository.setUserLocked(username, true);

        // Act
        Boolean userLockStatus = userRepository.isUserLocked(username);

        // Assert
        assertTrue(userLockStatus, "user locked status should be true");
    }

    @Test
    void isUserLocked_userIsNotLocked_returnFalse(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        userRepository.save(username, email, passwordHash);
        userRepository.setUserLocked(username, true); // false by default
        userRepository.setUserLocked(username, false);

        // Act
        Boolean userLockStatus = userRepository.isUserLocked(username);

        // Assert
        assertFalse(userLockStatus, "user locked status should be false");
    }

    @Test
    void isUserLocked_byDefault_returnFalse(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        userRepository.save(username, email, passwordHash);

        // Act
        Boolean userLockStatus = userRepository.isUserLocked(username);

        // Assert
        assertFalse(userLockStatus, "user locked status should be false by default");
    }

    @Test
    void isUserLocked_userNotFound_returnNull(){
        // Arrange
        String noneExistenceUser = "testuser";
        // Act
        Boolean userLockStatus = userRepository.isUserLocked(noneExistenceUser);

        // Assert
        assertNull(userLockStatus, "user not found, lock status should be null");
    }

    @Test
    void setUserLocked_setUserLockedToTrue(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        userRepository.save(username, email, passwordHash);

        // Act
        userRepository.setUserLocked(username, true);

        // Assert
        assertTrue(userRepository.isUserLocked(username), "User locked status should be true");
    }

    @Test
    void setUserLocked_setUserLockedToFalse(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        userRepository.save(username, email, passwordHash);

        // Act
        userRepository.setUserLocked(username, false);

        // Assert
        assertFalse(userRepository.isUserLocked(username), "User locked status should be false");
    }

    @Test
    void setUserLocked_nonExistenceUser_throwsException(){
        assertThrows(UserNotFoundException.class, () -> userRepository.setUserLocked("nonexistenteuser", true));
    }
}
