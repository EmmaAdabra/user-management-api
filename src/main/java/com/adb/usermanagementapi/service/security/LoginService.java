package com.adb.usermanagementapi.service.security;

import com.adb.usermanagementapi.dto.request.LoginRequestDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;

public interface LoginService {
    UserResponseDTO login(LoginRequestDTO dto);
}
