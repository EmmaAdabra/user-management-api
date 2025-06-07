package com.adb.usermanagementapi.exception;

public class UserAccountLockedException extends LoginException{
    public UserAccountLockedException(String message) {
        super(message);
    }
}