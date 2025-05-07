package com.adb.usermanagementapi.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message)
    {
        super(message);
    }

    public UserNotFoundException()
    {
        super("User not found in database");
    }
}
