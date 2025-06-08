package com.adb.usermanagementapi.mapper;

import com.adb.usermanagementapi.dto.request.UserCreateRequestDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;
import com.adb.usermanagementapi.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {
    public UserResponseDTO toUserResponseDTO(User user){
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isLocked(),
                user.getCreatedAt()
        );
    }

    public User toUser(UserCreateRequestDTO userCreateRequestDTO, String passwordHash){
        return new User(
                null,
                userCreateRequestDTO.getUsername(),
                userCreateRequestDTO.getEmail(),
                passwordHash,
                LocalDateTime.now(),
                false
        );
    }
    public User toUser(UserCreateRequestDTO userCreateRequestDTO){
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
