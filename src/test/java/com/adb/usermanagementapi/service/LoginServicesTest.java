package com.adb.usermanagementapi.service;

import com.adb.usermanagementapi.config.TestLoginServiceConfig;
import com.adb.usermanagementapi.dto.request.LoginRequestDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;
import com.adb.usermanagementapi.exception.InvalidLoginCredentialsException;
import com.adb.usermanagementapi.exception.InvalidPassword;
import com.adb.usermanagementapi.exception.UserAccountLockedException;
import com.adb.usermanagementapi.exception.UserNotFoundException;
import com.adb.usermanagementapi.mapper.UserMapper;
import com.adb.usermanagementapi.model.User;
import com.adb.usermanagementapi.model.login.LoginAttempt;
import com.adb.usermanagementapi.repository.LoginAttemptsRepository;
import com.adb.usermanagementapi.repository.UserRepository;
import com.adb.usermanagementapi.service.security.LoginService;
import com.adb.usermanagementapi.service.security.PasswordValidator;
import com.adb.usermanagementapi.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(TestLoginServiceConfig.class)
public class LoginServicesTest {
    private static final int LOCKED_INTERVAL = 1; // in minute
    @Autowired
    private LoginService loginService;

    @Autowired
    private LoginAttemptsRepository loginAttemptsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordValidator passwordValidator;

    private LoginRequestDTO createLoginRequestDTO(String email, String password){
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setPlainPassword(password);
        return dto;
    }

    @BeforeEach
    void resetMocks() {
        reset(userRepository, loginAttemptsRepository, userMapper);
    }

    @Test
    void login_success_returnsUserResponseDTO(){
        // Arrange
        String email = "user@example.com";
        String plainPassword = "plainPassword_123";

        LoginRequestDTO loginRequestDTO = createLoginRequestDTO(email, plainPassword);

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

        User user = TestUtils.getUser(email, hashedPassword, false);

        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(loginAttemptsRepository).saveLoginAttempt(anyLong(), eq(true));

        UserResponseDTO expectedResult = new UserResponseDTO(1L, "username", email, false,
                LocalDateTime.now());
        when(userMapper.toUserResponseDTO(user)).thenReturn(expectedResult);

        // Acct
        UserResponseDTO result = loginService.login(loginRequestDTO);

        // Assert
        assertEquals(expectedResult, result, "expected and result should be the same");
        assertEquals(loginRequestDTO.getEmail(), result.getEmail(), "Request email and response " +
                "email should be the same");
        verify(userRepository).findByEmail(loginRequestDTO.getEmail());
        verify(loginAttemptsRepository).saveLoginAttempt(user.getId(), true);
    }

    @Test
    void login_noneExistingEmail_throwsException(){
        // Arrange
        String noneExistingEmail = "none_existing_email@example.com";
        String plainPassword = "plainPassword_123";

        LoginRequestDTO loginRequestDTO = createLoginRequestDTO(noneExistingEmail, plainPassword);

        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.ofNullable(null));

        // Act and Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> loginService.login(loginRequestDTO));

        assertEquals("No user found with this email", ex.getMessage());
    }

    @Test
    void login_withWrongPassword_throwException(){
        String email = "user@example.com";
        String wrongPassword = "wrongPassword_123";

        LoginRequestDTO loginRequestDTO = createLoginRequestDTO(email, wrongPassword);

        String hashedPassword = BCrypt.hashpw("correctPassword_123", BCrypt.gensalt(12));

        User user = TestUtils.getUser(email, hashedPassword, false);

        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(user));
        doThrow(new InvalidPassword("Invalid password")).when(passwordValidator).vaidate(any(User.class),
                eq(wrongPassword));
        doNothing().when(loginAttemptsRepository).saveLoginAttempt(user.getId(), false);

        List<LoginAttempt> loginAttempts = new  ArrayList<>(List.of(new LoginAttempt(1L, 1L, LocalDateTime.now(),
                false)));

        when(loginAttemptsRepository.findRecentLogins(user.getId(), LOCKED_INTERVAL)).thenReturn(loginAttempts);

        InvalidLoginCredentialsException ex = assertThrows(InvalidLoginCredentialsException.class
                , () -> loginService.login(loginRequestDTO));

        assertEquals("Invalid login details, 3 trial left", ex.getMessage());
        verify(userRepository).findByEmail(loginRequestDTO.getEmail());
        verify(loginAttemptsRepository).findRecentLogins(user.getId(), LOCKED_INTERVAL);
        verify(loginAttemptsRepository).saveLoginAttempt(user.getId(), false);
        verify(passwordValidator).vaidate(any(User.class),
                eq(wrongPassword));
    }

    @Test
    void login_failedTrialLimitExceed_LockAccount_throwException(){
        String email = "user@example.com";
        String wrongPassword = "wrongPassword_123";

        LoginRequestDTO loginRequestDTO = createLoginRequestDTO(email, wrongPassword);
        String hashedPassword = BCrypt.hashpw("correctPassword_123", BCrypt.gensalt(12));

        User user = TestUtils.getUser(email, hashedPassword, false);

        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(loginAttemptsRepository).saveLoginAttempt(user.getId(), false);

        List<LoginAttempt> loginAttempts = new  ArrayList<>(List.of(
                new LoginAttempt(1L, 1L, LocalDateTime.now(),
                false),
                new LoginAttempt(1L, 1L, LocalDateTime.now(),
                        false),
                new LoginAttempt(1L, 1L, LocalDateTime.now(),
                        false),
                new LoginAttempt(1L, 1L, LocalDateTime.now(),
                        false)
                ));

        when(loginAttemptsRepository.findRecentLogins(user.getId(), LOCKED_INTERVAL)).thenReturn(loginAttempts);

        UserAccountLockedException ex = assertThrows(UserAccountLockedException.class
                , () -> loginService.login(loginRequestDTO));

        assertEquals("Too many failed login attempts, account locked for 1 minute", ex.getMessage());
        assertTrue(user.isLocked(), "Account should be lock");
        verify(userRepository).findByEmail(loginRequestDTO.getEmail());
        verify(loginAttemptsRepository).findRecentLogins(user.getId(), LOCKED_INTERVAL);
        verify(loginAttemptsRepository).saveLoginAttempt(user.getId(), false);
    }

    @Test
    void login_success_unlockAcct_LockTimeElapse_returnsUserResponseDTO(){
        // Arrange
        String email = "user@example.com";
        String plainPassword = "plainPassword_123";

        LoginRequestDTO loginRequestDTO = createLoginRequestDTO(email, plainPassword);

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

        boolean isLocked = true;
        User user = TestUtils.getUser(email, hashedPassword, isLocked);

        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(user));

        Timestamp lastFailedLogin = Timestamp.valueOf(LocalDateTime.now().minusMinutes(2));
       when(loginAttemptsRepository.findLastFailedLoginAttempt(user.getId())).thenReturn(lastFailedLogin);
       doNothing().when(userRepository).setUserLocked(user.getId(), false);

        UserResponseDTO expectedResult = new UserResponseDTO(1L, "username", email, false,
                LocalDateTime.now());
        when(userMapper.toUserResponseDTO(user)).thenReturn(expectedResult);

        UserResponseDTO result = loginService.login(loginRequestDTO);

        assertEquals(expectedResult, result, "expected and result should be the same");
        assertFalse(user.isLocked(), "user account should not be locked");
        verify(userRepository).findByEmail(loginRequestDTO.getEmail());
        verify(loginAttemptsRepository).findLastFailedLoginAttempt(user.getId());
        verify(loginAttemptsRepository).saveLoginAttempt(user.getId(), true);
    }

    @Test
    void login__lockedAcct_LockTimeNotElapse_throws(){
        // Arrange
        String email = "user@example.com";
        String plainPassword = "plainPassword_123";

        LoginRequestDTO loginRequestDTO = createLoginRequestDTO(email, plainPassword);

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

        boolean isLocked = true;
        User user = TestUtils.getUser(email, hashedPassword, isLocked);

        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(user));

        Timestamp lastFailedLogin = Timestamp.valueOf(LocalDateTime.now().minusSeconds(30));
        when(loginAttemptsRepository.findLastFailedLoginAttempt(user.getId())).thenReturn(lastFailedLogin);

        UserAccountLockedException ex = assertThrows(UserAccountLockedException.class,
                () -> loginService.login(loginRequestDTO));

        assertEquals("Account locked, try again later", ex.getMessage());
        assertTrue(user.isLocked(), "user account should be locked");
        verify(userRepository).findByEmail(loginRequestDTO.getEmail());
        verify(loginAttemptsRepository).findLastFailedLoginAttempt(user.getId());
        verify(userRepository, never()).setUserLocked(anyLong(), eq(false));
    }
}
