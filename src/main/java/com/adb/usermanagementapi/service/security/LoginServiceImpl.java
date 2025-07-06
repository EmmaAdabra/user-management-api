package com.adb.usermanagementapi.service.security;

import com.adb.usermanagementapi.dto.request.LoginRequestDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;
import com.adb.usermanagementapi.exception.InvalidLoginCredentialsException;
import com.adb.usermanagementapi.exception.InvalidPasswordException;
import com.adb.usermanagementapi.exception.UserAccountLockedException;
import com.adb.usermanagementapi.exception.UserNotFoundException;
import com.adb.usermanagementapi.mapper.UserMapper;
import com.adb.usermanagementapi.model.User;
import com.adb.usermanagementapi.model.login.LoginAttempt;
import com.adb.usermanagementapi.repository.LoginAttemptsRepository;
import com.adb.usermanagementapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginServiceImpl implements LoginService {
    private static final int LOCKED_INTERVAL = 1; // in minutes
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 4;
    private final UserRepository userRepository;
    private final LoginAttemptsRepository loginAttemptsRepository;
    private final UserMapper userMapper;
    private final PasswordHasher passwordValidator;
    private final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    public LoginServiceImpl(
            LoginAttemptsRepository loginAttemptsRepository,
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordHasher passwordValidator
    )
    {
        this.userRepository = userRepository;
        this.loginAttemptsRepository = loginAttemptsRepository;
        this.userMapper = userMapper;
        this.passwordValidator = passwordValidator;
    }

    @Override
    public UserResponseDTO login(LoginRequestDTO dto) {
        User user =
                userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> {
                    logger.warn("{} - not a registered user", dto.getEmail());

                    return new UserNotFoundException(dto.getEmail() + " - not a registered user");
                });

        if(user.isLocked()){
            checkAndUnlockIfEligible(user);

            if(user.isLocked()){
                logger.warn("User account locked - user ID: {}", user.getId());
                throw new UserAccountLockedException("Account locked, try again later");
            }
        }

        validatePassword(user, dto.getPassword());
        loginAttemptsRepository.saveLoginAttempt(user.getId(), true);
        logger.info("Login successful - user ID: {}", user.getId());
        return userMapper.toUserResponseDTO(user);
    }

    private void checkAndUnlockIfEligible(User user){
        LocalDateTime lastFailedLogin = loginAttemptsRepository
                .findLastFailedLoginAttempt(user.getId()).toLocalDateTime();
        LocalDateTime maxLockedTime = LocalDateTime.now().minusMinutes(LOCKED_INTERVAL);

        if(lastFailedLogin.isBefore(maxLockedTime)){
            user.setLocked(false);
            logger.info("Account unlocked successfully - user ID: {}", user.getId());
            userRepository.setUserLocked(user.getId(), false);
        }
    }

    private void validatePassword(User user, String plainPassword){
        try {
            passwordValidator.validate(user, plainPassword);
        } catch (InvalidPasswordException e){
            loginAttemptsRepository.saveLoginAttempt(user.getId(), false);
            int failedLoginCount = enforceAccountLockOnLimitExceeded(user);
            int remainingTrial = MAX_FAILED_LOGIN_ATTEMPTS - failedLoginCount;
            logger.warn("Wrong email or password, {} trial left - user ID: {}", remainingTrial,
                    user.getId());
            throw new InvalidLoginCredentialsException("Invalid login details, " + remainingTrial + " trial left");
        }
    }

    private int getConservativeFailedLoginCount(User user){
        List<LoginAttempt> loginAttempts =  loginAttemptsRepository.findRecentLogins(user.getId()
                , LOCKED_INTERVAL);

        int conservativeFailedLoginCount = 0;

        for (LoginAttempt loginAttempt : loginAttempts) {
            if (loginAttempt.success()) {
                break;
            }
            conservativeFailedLoginCount++;
        }

        return conservativeFailedLoginCount;
    }

    private int enforceAccountLockOnLimitExceeded(User user){
        int failedLoginCount = getConservativeFailedLoginCount(user);

        if(failedLoginCount >= MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setLocked(true);
            userRepository.setUserLocked(user.getId(), true);
            logger.warn("Too many failed login attempt, account locked for 1 minutes - user ID: " +
                    "{}", user.getId());
            throw new UserAccountLockedException("Too many failed login attempts, " +
                    "account locked for 1 minute");

        }

        return failedLoginCount;
    }
}
