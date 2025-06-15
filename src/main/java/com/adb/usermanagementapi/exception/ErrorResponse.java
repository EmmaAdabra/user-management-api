package com.adb.usermanagementapi.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponse(String errorCode, String message, LocalDateTime timestamp) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
