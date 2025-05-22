package com.adb.usermanagementapi.service;

import com.adb.usermanagementapi.config.TestUserServiceConfig;
import com.adb.usermanagementapi.dto.request.UserCreateRequestDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;
import com.adb.usermanagementapi.exception.DuplicateResourceException;
import com.adb.usermanagementapi.model.User;
import com.adb.usermanagementapi.model.UserMapper;
import com.adb.usermanagementapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(TestUserServiceConfig.class)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserMapper userMapper;

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
        requestDTO.setPlainPassword(password);

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
        requestDTO.setPlainPassword("plain_password");
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
}
