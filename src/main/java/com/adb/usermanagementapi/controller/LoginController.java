package com.adb.usermanagementapi.controller;

import com.adb.usermanagementapi.dto.request.LoginRequestDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;
import com.adb.usermanagementapi.service.security.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final LoginService loginService;
    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody
             LoginRequestDTO request, HttpServletRequest servletRequest)
    {
        String ip = servletRequest.getRemoteAddr();
        String url = servletRequest.getRequestURI();
        String httpMethod = servletRequest.getMethod();

        logger.info("Login attempt - IP: {}, method: {}, URL: {}, email: {}", ip, httpMethod, url
                , request.getEmail());
        UserResponseDTO response = loginService.login(request);

        return ResponseEntity.ok(response);
    }
}
