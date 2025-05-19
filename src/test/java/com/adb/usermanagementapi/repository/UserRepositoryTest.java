package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.config.TestConfig;
import com.adb.usermanagementapi.exception.UserNotFoundException;
import com.adb.usermanagementapi.model.User;
import com.adb.usermanagementapi.util.TestUtils;
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
        User testUser = TestUtils.getUser(username, email, passwordHash);
        // Act
        userRepository.save(testUser);

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
        User testUser = TestUtils.getUser(username, email, passwordHash);

        // Act
        userRepository.save(testUser);

        // Assert
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () ->
            userRepository.save(testUser), "Should throw exception for duplicate username");
    }

    @Test
    void saveUser_duplicateEmail_throwsException(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        User testUser = TestUtils.getUser(username, email, passwordHash);

        // Act
        userRepository.save(testUser);

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
                User differentUser = TestUtils.getUser("differrentuser", email, passwordHash);
                userRepository.save(differentUser);},
                "Should throw an exception, email already exist");
    }

    @Test
    void userExistByUsername_userExist_returnsTrue(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        User user = TestUtils.getUser(username, email, passwordHash);

        User testUser = userRepository.save(user);

        // Act
        assertTrue(userRepository.existsByUsername(testUser.getUsername()), "should return true, user exist by" +
                " " +
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
        User user = TestUtils.getUser(username, email, passwordHash);

        User testUser = userRepository.save(user);

        // Act
        assertTrue(userRepository.existsByEmail(testUser.getEmail()), "should return true, " +
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
        User user = TestUtils.getUser(username, email, passwordHash);

        User testUser = userRepository.save(user);

        //Act
        Long userId = userRepository.findIdByUsername(testUser.getUsername());

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
        User user = TestUtils.getUser(username, email, passwordHash);

        User testUser = userRepository.save(user);

        //Act
        Long userId = userRepository.findIdByEmail(testUser.getEmail());

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
        User user = TestUtils.getUser(username, email, passwordHash);

        userRepository.save(user);

        // Act
        User savedUser = userRepository.findByUsername(username);

        assertNotNull(savedUser, "User should be found");
        assertEquals(username, savedUser.getUsername(), "username should match");
        assertEquals(email, savedUser.getEmail(), "email should match");
        assertEquals(passwordHash, savedUser.getPasswordHash(), "passwordHash should match");
        assertNotNull(savedUser.getCreatedAt(), "Created at should be set");
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
        userRepository.save(TestUtils.getUser("user1", "user1@example.com", "passwordhash1"));
        userRepository.save(TestUtils.getUser("user2", "user2@example.com", "passwordhash2"));

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
    void updateUser_success_returnsTrue() {
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        User testUser = userRepository.save(TestUtils.getUser(username, email, passwordHash));

        String updatedUserName = "newuser";
        String updatedUserEmail = "newuser@gmail.com";

        // Act
        boolean isUpdated = userRepository.updateUser(updatedUserName, updatedUserEmail,
                testUser.getId());

        User foundUser = userRepository.findByUsername(updatedUserName);

        // Assert
        assertTrue(isUpdated, "isUpdated should be true");
        assertNull(userRepository.findByUsername(username), "Old username should no longer be found");
        assertNotNull(foundUser, "Updated user should be found");
        assertEquals(updatedUserName, foundUser.getUsername(), "Username should be updated");
        assertEquals(updatedUserEmail, foundUser.getEmail(), "Email should be updated");
        assertEquals(passwordHash, foundUser.getPasswordHash(), "Password hash should remain unchanged");
    }

    @Test
    void updateUser_userNotFound_returnsFalse(){
        // Arrange
        Long noneExistenceUserId = 90L;

        // Act
        boolean isUpdated = userRepository.updateUser("noneuser", "noneuser@gmail.com",
                noneExistenceUserId);

        assertFalse(isUpdated,"Should be false for a none user");
    }

    @Test
    void updatePassword_success_returnsTrue(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        User user = TestUtils.getUser(username, email, passwordHash);

        userRepository.save(user);

        String updatedPassword = "newHashPassword";

        // Act
        boolean isUpdated = userRepository.updatePassword(username, updatedPassword);

        // Assert
        assertTrue(isUpdated, "isUpdated should be true");
    }

    @Test
    void updatePassword_userNotFound_returnsFalse() {
        boolean isUpdated = userRepository.updatePassword("noneuser", "newhashedpassword");

        assertFalse(isUpdated, "isUpdated should be for false for none user");
    }

    @Test
    void deleteByUsername_success_returnsTrue() {
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        User user = TestUtils.getUser(username, email, passwordHash);

        userRepository.save(user);

        // Act
        boolean isDeleted = userRepository.deleteByUsername(username);

        // Assert
        assertTrue(isDeleted, "IsDeleted should be true");
        assertFalse(userRepository.existsByUsername(username), "User should be deleted");
        assertFalse(userRepository.existsByEmail(email), "User email should be deleted");
    }

    @Test
    void deleteByUsername_userNotFound_returnsFalse() {
        boolean isDeleted = userRepository.deleteByUsername("noneuser");

        assertFalse(isDeleted, "isDeleted should be false for none user");
    }

    @Test
    void isUserLocked_userIsLocked_returnsTrue(){
        // Arrange
        String username = "lockeduser";
        String email = "lockeduser@example.com";
        String passwordHash = "hashedpassword";
        User user = TestUtils.getUser(username, email, passwordHash);

        userRepository.save(user);
        userRepository.setUserLocked(username, true);

        // Act
        Boolean userLockStatus = userRepository.isUserLocked(username);

        // Assert
        assertTrue(userLockStatus, "user locked status should be true");
    }

    @Test
    void isUserLocked_userIsNotLocked_returnsFalse(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        User testUser = userRepository.save(TestUtils.getUser(username, email, passwordHash));

        userRepository.setUserLocked(testUser.getUsername(), true); // false by default
        userRepository.setUserLocked(testUser.getUsername(), false);

        // Act
        Boolean userLockStatus = userRepository.isUserLocked(username);

        // Assert
        assertFalse(userLockStatus, "user locked status should be false");
    }

    @Test
    void isUserLocked_byDefault_returnsFalse(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";
        userRepository.save(TestUtils.getUser(username, email, passwordHash));

        // Act
        Boolean userLockStatus = userRepository.isUserLocked(username);

        // Assert
        assertFalse(userLockStatus, "user locked status should be false by default");
    }

    @Test
    void isUserLocked_userNotFound_returnsNull(){
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

        User testUser = userRepository.save(TestUtils.getUser(username, email, passwordHash));

        // Act
        userRepository.setUserLocked(testUser.getUsername(), true);

        // Assert
        assertTrue(userRepository.isUserLocked(username), "User locked status should be true");
    }

    @Test
    void setUserLocked_setUserLockedToFalse(){
        // Arrange
        String username = "testuser";
        String email = "testuser@example.com";
        String passwordHash = "hashedpassword";

        User testUser = userRepository.save(TestUtils.getUser(username, email, passwordHash));

        // Act
        userRepository.setUserLocked(testUser.getUsername(), false);

        // Assert
        assertFalse(userRepository.isUserLocked(username), "User locked status should be false");
    }

    @Test
    void setUserLocked_nonExistenceUser_throwsException(){
        assertThrows(UserNotFoundException.class, () -> userRepository.setUserLocked("nonexistenteuser", true));
    }
}
