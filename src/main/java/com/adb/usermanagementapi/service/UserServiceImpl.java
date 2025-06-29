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

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           PasswordHasher passordHasher) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordHasher = passordHasher;
    }

    @Override
    public UserResponseDTO createUser(UserCreateRequestDTO request) {
        if(userRepository.existsByUsername(request.getUsername())){
            throw new DuplicateResourceException("Username already exist");
        }

        if(userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Email already exist");
        }

        // hash password
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12));
        User newUser = userMapper.toUser(request, hashedPassword);
        User savedUser = userRepository.save(newUser);
        return userMapper.toUserResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserUpdateDTO request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(userRepository.existsByUsername(request.getUsername())
                && !existingUser.getUsername().equals(request.getUsername())){
            throw new DuplicateResourceException("Username already exist");
        }

        if(userRepository.existsByEmail(request.getEmail())
                && !existingUser.getEmail().equals(request.getEmail())){
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

        return userMapper.toUserResponseDTO(updatedUser);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers(int page, int size) {
        page = page == 1 ? 0 : page - 1;
        return userRepository.findAll(page, size).stream().map(userMapper::toUserResponseDTO).collect(
                Collectors.toList());
    }

    @Override
    public void updatePassword(Long id, ChangePasswordRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        try {
            passwordHasher.validate(user, dto.getOldPassword());
        } catch (InvalidPasswordException e) {
            throw new InvalidCurrentPasswordException("Current password mismatch");
        }

        //if (dto.getOldPassword().equals(dto.getNewPassword())) {//}

        String hashedPassword = passwordHasher.hashPassword(dto.getNewPassword());
        System.out.println("Test value: " + dto.getNewPassword());
        System.out.println(hashedPassword);
        userRepository.updatePassword(id, hashedPassword);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.deleteUser(id);
    }
}
