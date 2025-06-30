package com.adb.usermanagementapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangePasswordRequestDTO {
    @NotBlank
    private String oldPassword;

    @NotBlank(message = "Username must not be blank")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+=\\\\\\\\-{}:;\"'<>,.?/])" +
            ".{8,}$",
            message = "Password must be at least 8 characters long, include one uppercase, one " +
                    "number and one special character"
    )
    private String newPassword;

    @Override
    public String toString() {
        return "ChangePasswordRequestDTO{" +
                "oldPassword='" + "********" + '\'' +
                ", newPassword='" + "********" + '\'' +
                '}';
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
