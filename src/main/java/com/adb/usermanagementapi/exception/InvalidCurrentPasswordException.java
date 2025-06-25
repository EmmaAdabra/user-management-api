package com.adb.usermanagementapi.exception;

public class InvalidCurrentPasswordException extends RuntimeException{
    public InvalidCurrentPasswordException(String message){
        super(message);
    }
}
