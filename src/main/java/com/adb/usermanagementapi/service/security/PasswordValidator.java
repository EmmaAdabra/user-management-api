package com.adb.usermanagementapi.service.security;

import com.adb.usermanagementapi.exception.InvalidPasswordException;
import com.adb.usermanagementapi.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
    public void validate(User user, String plainPassword){
        if(!BCrypt.checkpw(plainPassword, user.getPasswordHash())){
            throw new InvalidPasswordException("Invalid password");
        }
    }
}
