package com.adb.usermanagementapi.service;

import com.adb.usermanagementapi.config.TestUserServiceConfig;
import com.adb.usermanagementapi.dto.request.ChangePasswordRequestDTO;
import com.adb.usermanagementapi.dto.request.UserCreateRequestDTO;
import com.adb.usermanagementapi.dto.request.UserUpdateDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;
import com.adb.usermanagementapi.exception.DuplicateResourceException;
import com.adb.usermanagementapi.exception.InvalidCurrentPasswordException;
import com.adb.usermanagementapi.exception.UserNotFoundException;
import com.adb.usermanagementapi.model.User;
import com.adb.usermanagementapi.mapper.UserMapper;
import com.adb.usermanagementapi.repository.UserRepository;
import com.adb.usermanagementapi.service.security.PasswordHasher;
import com.adb.usermanagementapi.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(TestUserServiceConfig.class)
public class UserServiceTest {
    private final int PAGE = 0;
    private final int SIZE = 10;
    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private PasswordHasher passwordHasher;

    @Test
    void createUser_success_returnsUserResponseDTO(){
        // Arrange
        String username = "test_user";
        String email = "testuser@example.com";
        String password = "Plain_password123";
        LocalDateTime createdAt = LocalDateTime.now();
        UserCreateRequestDTO requestDTO = new UserCreateRequestDTO();

        requestDTO.setUsername(username);
        requestDTO.setEmail(email);
        requestDTO.setPassword(password);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User newUser = new User(null, username, email, hashedPassword, null, false);
        User savedUser = new User(1L, username, email, hashedPassword, createdAt, false);

        when(userMapper.toUser(eq(requestDTO), anyString())).thenReturn(newUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO expectedResponse = new UserResponseDTO(1L, username, email, false,
                createdAt);
        when(userMapper.toUserResponseDTO(savedUser)).thenReturn(expectedResponse);

        // Act
        UserResponseDTO result = userService.createUser(requestDTO);

        // Assert
        assertEquals(expectedResponse, result, "Expected user response DTO should be the same " +
                "with result DTO");
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository).save(newUser);
    }

    @Test
    void createUser_duplicateUserName_throwsException(){
        // Arrange
        UserCreateRequestDTO requestDTO = new UserCreateRequestDTO();
        requestDTO.setUsername("existing_user");
        requestDTO.setEmail("test_user@example.com");
        requestDTO.setPassword("plain_password");
        when(userRepository.existsByUsername(requestDTO.getUsername())).thenReturn(true);

        // Act
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(requestDTO));

        // Assert
        assertEquals("Username already exist", ex.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail_throwsException(){
        UserCreateRequestDTO requestDTO = new UserCreateRequestDTO();
        requestDTO.setUsername("test_user");
        requestDTO.setEmail("existing_email@example.com");

        when(userRepository.existsByUsername(requestDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(requestDTO));

        assertEquals("Email already exist", ex.getMessage());
        verify(userRepository).existsByUsername(requestDTO.getUsername());
        verify(userRepository).existsByEmail(requestDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_success_returnsResponseDTO(){
        // Arrange
        String newUsername = "new_username";
        String newEmail = "new_username@example.com";
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();

        userUpdateDTO.setUsername(newUsername);
        userUpdateDTO.setEmail(newEmail);

        Long existingUserId = 1L;

        User existingUser = new User(existingUserId,
                "old_username",
                "old_username@example.com",
             "@00#@00%",
                LocalDateTime.now(),
                false
        );

        User updatedUser = new User(
                existingUserId,
                newUsername,
                newEmail,
                existingUser.getPasswordHash(),
                existingUser.getCreatedAt(),
                false
        );

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.updateUser(updatedUser)).thenReturn(true);

        UserResponseDTO expectedResponse = new UserResponseDTO(1L, updatedUser.getUsername(),
                updatedUser.getEmail(),  false, updatedUser.getCreatedAt());
        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(expectedResponse);

        // Act
       UserResponseDTO result = userService.updateUser(existingUserId, userUpdateDTO);

        // Assert
        assertEquals(expectedResponse, result, "The expected response object should be the same " +
                "with the result");
        verify(userRepository).findById(existingUserId);
        verify(userRepository).existsByUsername(newUsername);
        verify(userRepository).existsByEmail(newEmail);
        verify(userRepository).updateUser(any(User.class));
    }

    @Test
    void updateUser_invalidUserId_throwsException() {
        // Arrange
        Long invalidId = 90L;
        when(userRepository.findById(invalidId)).thenReturn(Optional.ofNullable(null));

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setUsername("test_user");
        userUpdateDTO.setEmail("test_user@example.com");

        // Act
        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(invalidId, userUpdateDTO));

        // Assert
        assertEquals("User not found", ex.getMessage(), "error message should be 'User not found'");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).updateUser(any(User.class));
    }

    @Test
    void updateUser_duplicateUserName_throwsException(){
        // Arrange
        String newUsername = "some_existingUsername";
        String newEmail = "user_email@example.com";
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();

        userUpdateDTO.setUsername(newUsername);
        userUpdateDTO.setEmail(newEmail);

        Long existingUserId = 1L;

        User existingUser = new User(existingUserId,
                "old_username",
                "old_username@example.com",
                "@00#@00%",
                LocalDateTime.now(),
                false);


        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername(newUsername)).thenReturn(true);

        // Act and Assert
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> userService.updateUser(existingUserId, userUpdateDTO));

        assertEquals("Username already exist", ex.getMessage(), "should throw exception duplicate" +
                " username");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).updateUser(any(User.class));
    }

    @Test
    void updateUser_duplicateEmail_throwsException(){
        // Arrange
        String newUsername = "newUsername";
        String newEmail = "some_existing_email@example.com";
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();

        userUpdateDTO.setUsername(newUsername);
        userUpdateDTO.setEmail(newEmail);

        Long existingUserId = 1L;

        User existingUser = new User(existingUserId,
                "old_username",
                "old_username@example.com",
                "@00#@00%",
                LocalDateTime.now(),
                false);


        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        // Act and Assert
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> userService.updateUser(existingUserId, userUpdateDTO));

        assertEquals("Email already exist", ex.getMessage(), "should throw exception for " +
                "duplicated email");
        verify(userRepository, never()).updateUser(any(User.class));
    }

    @Test
    void getUserById_existingUser_returnsUserResponseDTO(){
        // Arrange
        Long existingUserId = 1L;
        User user = new User(
                existingUserId,
                "username",
                "username@example.com",
                "hashedPassword",
                LocalDateTime.now(),
                false
        );

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(user));

        UserResponseDTO expectedResponse = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isLocked(),
                user.getCreatedAt()
        );

        when(userMapper.toUserResponseDTO(user)).thenReturn(expectedResponse);

        // Act
        UserResponseDTO result = userService.getUserById(existingUserId);

        // Assert
        assertEquals(expectedResponse, result);
        verify(userRepository).findById(existingUserId);
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    void getUserById_userNotFound_throwsException(){
        Long invalidUserId = 90L;

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.ofNullable(null));

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(invalidUserId));

        assertEquals("User not found", ex.getMessage());
        verify(userRepository).findById(invalidUserId);
        verify(userMapper, never()).toUserResponseDTO(any(User.class));
    }

    @Test
    void getAllUsers_returnsListOfUserResponseDTO(){
        // Arrange
        User user1 = new User(
                1L,
                "username1",
                "username1@example.com",
                "hashedPassword",
                LocalDateTime.now(),
                false
        );

        User user2 = new User(
                2L,
                "username2",
                "username2@example.com",
                "hashedPassword",
                LocalDateTime.now(),
                false
        );

        UserResponseDTO dto1 = new UserResponseDTO(
                user1.getId(),
                user1.getUsername(),
                user1.getEmail(),
                user1.isLocked(),
                user1.getCreatedAt()
        );

        UserResponseDTO dto2 = new UserResponseDTO(
                user2.getId(),
                user2.getUsername(),
                user2.getEmail(),
                user2.isLocked(),
                user2.getCreatedAt()
        );

        List<User> userList = List.of(user1,user2);

        when(userRepository.findAll(PAGE, SIZE)).thenReturn(userList);
        when(userMapper.toUserResponseDTO(user1)).thenReturn(dto1);
        when(userMapper.toUserResponseDTO(user2)).thenReturn(dto2);

        // Act
        List<UserResponseDTO> result = userService.getAllUsers(PAGE, SIZE);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(userRepository).findAll(PAGE, SIZE);
        verify(userMapper).toUserResponseDTO(user1);
        verify(userMapper).toUserResponseDTO(user2);
    }

    @Test
    void getAllUsers_returnsEmptyList(){
        // Arrange
        when(userRepository.findAll(PAGE, SIZE)).thenReturn(Collections.emptyList());

        // Act
        List<UserResponseDTO> result = userService.getAllUsers(PAGE, SIZE);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findAll(PAGE, SIZE);
        verifyNoInteractions(userMapper);
    }

    @Test
    void updatePassword_success(){
        // Arrange
        Long userId = 1L;
        User mockUser = mock(User.class);
        ChangePasswordRequestDTO dto =  new ChangePasswordRequestDTO();
        dto.setNewPassword("newPassword123");
        dto.setOldPassword("oldPassword123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.updatePassword(eq(userId), anyString())).thenReturn(true);
        when(passwordHasher.hashPassword(anyString())).thenReturn("hashedPassword123");

        // Act
        userService.updatePassword(userId, dto);

        // Assert
        verify(userRepository).updatePassword(eq(userId), anyString());
        verify(userRepository).findById(userId);
    }

    @Test
    void updatePassword_invalidUserId_throwsException(){
        //Arrange
        Long noneExistingUserId = 90L;
        ChangePasswordRequestDTO dto =  new ChangePasswordRequestDTO();
        dto.setNewPassword("newPassword123");
        dto.setOldPassword("oldPassword123");

        when(userRepository.findById(noneExistingUserId)).thenReturn(Optional.ofNullable(null));

        //Act and assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.updatePassword(noneExistingUserId, dto));

        assertEquals("User not found", ex.getMessage());
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void updatePassword_withWrongCurrentPassword_throwsException(){
        //Arrange
        Long userId = 1L;
        String hashPassword = new PasswordHasher().hashPassword("oldPassword123");
        String wrongOldPassword = "wrongOldPassword123";
        User user = TestUtils.getUser("username", "user@example.com", hashPassword);

        ChangePasswordRequestDTO dto =  new ChangePasswordRequestDTO();
        dto.setNewPassword("newPassword123");
        dto.setOldPassword(wrongOldPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doThrow(new InvalidCurrentPasswordException("Current password mismatch")).when(passwordHasher).validate(user,
                dto.getOldPassword());
        //Act and assert
        InvalidCurrentPasswordException ex = assertThrows(InvalidCurrentPasswordException.class,
                () -> userService.updatePassword(userId, dto));

        assertEquals("Current password mismatch", ex.getMessage());
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void deleteUser_success(){
        // Arrange
        Long userId = 1L;
        User mockUser = mock(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.deleteUser(userId)).thenReturn(true);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).deleteUser(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void deleteUser_noneExistingUserId(){
        // Arrange
        Long noneExistingUserId = 90L;

        when(userRepository.findById(noneExistingUserId)).thenReturn(Optional.ofNullable(null));

        // Act and Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(noneExistingUserId));

        assertEquals("User not found", ex.getMessage());
        verify(userRepository).findById(noneExistingUserId);
        verify(userRepository, never()).deleteUser(anyLong());
    }
}
