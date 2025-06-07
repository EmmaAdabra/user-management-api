package com.adb.usermanagementapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {
    @NotBlank(message = "Username or email must not be blank")
    private String userEmail;
    @NotBlank(message = "Password should not be blank")
    private String plainPassword;

    // Getters and setters
    public String getUserEmail() {
        return userEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.userEmail = usernameOrEmail;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }
}
