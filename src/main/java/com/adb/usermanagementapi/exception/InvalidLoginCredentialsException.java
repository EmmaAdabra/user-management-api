package com.adb.usermanagementapi.exception;

public class InvalidLoginCredentialsException extends LoginException{
    public InvalidLoginCredentialsException(String message) {
        super(message);
    }
}
