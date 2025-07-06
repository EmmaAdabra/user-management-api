package com.adb.usermanagementapi.controller;

import com.adb.usermanagementapi.dto.request.ChangePasswordRequestDTO;
import com.adb.usermanagementapi.dto.request.UserCreateRequestDTO;
import com.adb.usermanagementapi.dto.request.UserUpdateDTO;
import com.adb.usermanagementapi.dto.response.UserResponseDTO;
import com.adb.usermanagementapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
   public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody UserCreateRequestDTO request,
            HttpServletRequest servletRequest
            ){
        String url = servletRequest.getRequestURI();
        String method = servletRequest.getMethod();

        logger.info("Registration attempt - IP: {}, method: {}, url: {}, email: '{}', username: '{}'",
                servletRequest.getRemoteAddr(), method, url, request.getEmail(),
                request.getUsername());
        UserResponseDTO response = userService.createUser(request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateDTO request,
            HttpServletRequest servletRequest
    )
    {
        String url = servletRequest.getRequestURI();
        String method = servletRequest.getMethod();

        logger.info("Update request - IP: {}, method: {},  url: {}, userId: {}, new username: '{}'," +
                        " new email: '{}'",
                servletRequest.getRemoteAddr(), method, url,  id, request.getUsername(),
                request.getEmail());
        UserResponseDTO response = userService.updateUser(id, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable("id") long id,
        HttpServletRequest servletRequest
    ){
        String url = servletRequest.getRequestURI();
        String method = servletRequest.getMethod();

        logger.info("Get user by ID request - IP: {}, method: {}, url: {}, userId: {}",
                servletRequest.getRemoteAddr(), method, url,  id);
        UserResponseDTO response = userService.getUserById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            HttpServletRequest servletRequest
    ){
        String url = servletRequest.getRequestURI();
        String method = servletRequest.getMethod();

        logger.info("Get all users request - IP: {}, method: {}, url: {}, page: {}, size: {}",
                servletRequest.getRemoteAddr(), method, url,  page, size);

        List<UserResponseDTO> response = userService.getAllUsers(page, size);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody ChangePasswordRequestDTO request,
            HttpServletRequest servletRequest
            ){
        String url = servletRequest.getRequestURI();
        String method = servletRequest.getMethod();

        logger.info("Change password attempt - IP: {}, method: {}, url: {}, userId: {}",
                servletRequest.getRemoteAddr(), method, url,  id);
        userService.updatePassword(id, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUSer(@PathVariable("id") Long id,
               HttpServletRequest servletRequest){
        String url = servletRequest.getRequestURI();
        String method = servletRequest.getMethod();

        logger.info("Delete request - IP: {}, method: {}, url: {}, userId: {}",
                servletRequest.getRemoteAddr(), method, url,  id);
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}