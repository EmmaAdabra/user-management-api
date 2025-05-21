package com.adb.usermanagementapi.service;

import com.adb.usermanagementapi.dto.request.UserCreateRequestDTO;
import com.adb.usermanagementapi.dto.request.UserUpdateDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserCreateRequestDTO request);
    UserResponseDTO updateUser(Long id, UserUpdateDTO request);
    UserResponseDTO getUserById(Long id);
    List<UserResponseDTO> getAllUsers();
    void updatePassword(Long id, String newPassword);
    void deleteUser(Long id);
}
