package com.adb.usermanagementapi.service;

import com.adb.usermanagementapi.dto.request.ChangePasswordRequestDTO;
import com.adb.usermanagementapi.dto.request.UserCreateRequestDTO;
import com.adb.usermanagementapi.dto.request.UserUpdateDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;
import com.adb.usermanagementapi.exception.DuplicateResourceException;
import com.adb.usermanagementapi.exception.InvalidCurrentPasswordException;
import com.adb.usermanagementapi.exception.InvalidPasswordException;
import com.adb.usermanagementapi.exception.UserNotFoundException;
import com.adb.usermanagementapi.model.User;
import com.adb.usermanagementapi.repository.UserRepository;
import com.adb.usermanagementapi.mapper.UserMapper;
import com.adb.usermanagementapi.service.security.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           PasswordHasher passordHasher) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordHasher = passordHasher;
    }

    @Override
    public UserResponseDTO createUser(UserCreateRequestDTO request) {
        logger.info("Attempting to register user with username - '{}'", request.getUsername());
        if(userRepository.existsByUsername(request.getUsername())){
            logger.warn("Registration failed, username - '{}', already exist",
                    request.getUsername());
            throw new DuplicateResourceException("Username already exist");
        }

        if(userRepository.existsByEmail(request.getEmail())){
            logger.warn("Registration failed, email - '{}', already exist", request.getEmail());
            throw new DuplicateResourceException("Email already exist");
        }

        // hash password
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12));
        User newUser = userMapper.toUser(request, hashedPassword);
        User savedUser = userRepository.save(newUser);
        logger.info("User registered successfully with ID - {}", savedUser.getId());
        return userMapper.toUserResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserUpdateDTO request) {
        logger.info("Attempting to update user data with ID - {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User update failed, no user found with ID - {}", id);
                    return new UserNotFoundException("User not found");
                });

        if(userRepository.existsByUsername(request.getUsername())
                && !existingUser.getUsername().equals(request.getUsername())){
            logger.warn("Update failed, username - '{}', already exist",
                    request.getUsername());
            throw new DuplicateResourceException("Username already exist");
        }

        if(userRepository.existsByEmail(request.getEmail())
                && !existingUser.getEmail().equals(request.getEmail())){
            logger.warn("Update failed, email - '{}', already exist", request.getEmail());
            throw new DuplicateResourceException("Email already exist");
        }

        User updatedUser = new User(
                id,
                request.getUsername(),
                request.getEmail(),
                existingUser.getPasswordHash(),
                existingUser.getCreatedAt(),
                existingUser.isLocked());

        userRepository.updateUser(updatedUser);
        logger.info("User updated successfully with ID - {}", id);

        return userMapper.toUserResponseDTO(updatedUser);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        logger.info("Attempting to get user with ID - {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Failed to get user, no user found with the ID - {}", id);
                    return new UserNotFoundException("User not found");
                });

        logger.info("Successfully get user with ID - {}", id);
        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers(int page, int size) {
        page = page == 1 ? 0 : page - 1;
        logger.info("Getting all users in page - {}", page);
        return userRepository.findAll(page, size).stream().map(userMapper::toUserResponseDTO).collect(
                Collectors.toList());
    }

    @Override
    public void updatePassword(Long id, ChangePasswordRequestDTO dto) {
        logger.info("Attempting to change user password with ID - {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Failed to change password, no user with the ID - {}", id);

                    return new UserNotFoundException("User not found");
                });

        try {
            passwordHasher.validate(user, dto.getOldPassword());
        } catch (InvalidPasswordException e) {
            logger.warn("Failed to change password, current password mismatch for user ID - {}",
                    id);
            throw new InvalidCurrentPasswordException("Current password mismatch");
        }

        //if (dto.getOldPassword().equals(dto.getNewPassword())) {//}

        String hashedPassword = passwordHasher.hashPassword(dto.getNewPassword());
        userRepository.updatePassword(id, hashedPassword);
        logger.info("Password changed successfully for user with ID - {}", id);
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Attempting to delete user with ID - {}", id);
        userRepository.findById(id).orElseThrow(() -> {
            logger.info("Failed to delete user, no user found with ID - {}", id);

            return new UserNotFoundException("User not found");
        });

        userRepository.deleteUser(id);
        logger.info("Successfully deleted user with ID - {}", id);
    }
}
