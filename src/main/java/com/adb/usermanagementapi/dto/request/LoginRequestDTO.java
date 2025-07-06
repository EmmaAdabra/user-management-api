package com.adb.usermanagementapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {
    @NotBlank(message = "Username or email must not be blank")
    private String email;
    @NotBlank(message = "Password should not be blank")
    private String password;

    @Override
    public String toString() {
        return "LoginRequestDTO{" +
                "email='" + email + '\'' +
                ", password='" + "********" + '\'' +
                '}';
    }

    // Getters and setters
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
