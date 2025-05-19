package com.adb.usermanagementapi.util;

import com.adb.usermanagementapi.dto.request.UserCreateRequestDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;
import com.adb.usermanagementapi.model.User;

import java.time.LocalDateTime;

public class UserMapper {
    public static UserResponseDTO toUserResponseDTO(User user){
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isLocked(),
                user.getCreatedAt()
        );
    }

    public static User toModel(UserCreateRequestDTO userCreateRequestDTO){
        return new User(
                null,
                userCreateRequestDTO.getUsername(),
                userCreateRequestDTO.getEmail(),
                userCreateRequestDTO.getPlainPassword(),
                LocalDateTime.now(),
                false
        );
    }
}
