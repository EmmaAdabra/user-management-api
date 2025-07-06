package com.adb.usermanagementapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceError(DuplicateResourceException ex){
        logger.warn("Duplicate resources error: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("DUPLICATE_RESOURCE", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestBody(HttpMessageNotReadableException ex){
        logger.warn("Bad request - {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("BAD_REQUEST_BODY", "Missing or malformed " +
                "request body");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleWrongHttpMethod(
            HttpRequestMethodNotSupportedException ex) {
        logger.warn("Wrong HTTP method - {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("WRONG_METHOD", "HTTP method not allowed");

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    public ResponseEntity<ErrorResponse> handleMissingRequestParam(MissingServletRequestParameterException ex){
        logger.info("Bad request - {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("MISSING_REQUEST_PARAM", "Missing required " +
                "query parameter: " + "'" + ex.getParameterName() + "'");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex){
        Map<String, String> fieldErrors = new HashMap<>();

        for(FieldError error : ex.getBindingResult().getFieldErrors()){
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        logger.warn("Validation error - {}", fieldErrors);

        ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", "Request validation " +
                "failed", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex){
        logger.warn("User not found - {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ErrorResponse> handleChangeOfPasswordMismatch(InvalidCurrentPasswordException ex){
        logger.warn("Invalid current password, change attempt - {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("PASSWORD_MISMATCH", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnExpectedError(Exception ex){
        logger.error("Unexpected error occurred - ", ex);
        ErrorResponse response = new ErrorResponse("INTERNAL_ERROR", "An unexpected error " +
                "occurred");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
