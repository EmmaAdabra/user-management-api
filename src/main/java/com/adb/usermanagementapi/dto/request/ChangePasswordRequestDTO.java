package com.adb.usermanagementapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangePasswordRequestDTO {
    @NotBlank
    private String oldPassword;

    @NotBlank(message = "Username must not be blank")
    @Pattern(regexp = "^(?![_.,-])[A-Za-z0-9._,-]+(?<![_.,-])$",
            message = "Username can contain letters, digits, '_', '.', ',' or '-', " +
                    "but not start or end with them"
    )
    private String newPassword;

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
