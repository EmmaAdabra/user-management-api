package com.adb.usermanagementapi.dto.request;

import jakarta.validation.constraints.*;

public class UserCreateRequestDTO {
    @NotBlank(message = "Username must not be blank")
    @Pattern(regexp = "^(?![_.,-])[A-Za-z0-9._,-]+(?<![_.,-])$",
            message = "Username can contain letters, digits, '_', '.', ',' or '-', " +
                    "but not start or end with them"
    )
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Should be a valid email")
    private String email;

    @NotBlank(message = "Password is should not be null")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+=\\\\\\\\-{}:;\"'<>,.?/])" +
            ".{8,}$",
            message = "Password must be at least 8 characters long, include one uppercase, one " +
                    "number and one special character"
    )
    @Size(max = 30, message = "password should not exceed 30 character")
    private String password;

    @Override
    public String toString() {
        return "UserCreateRequestDTO{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + "********" + '\'' +
                '}';
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
