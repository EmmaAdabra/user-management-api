package com.adb.usermanagementapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceError(DuplicateResourceException ex){
        ErrorResponse response = new ErrorResponse("DUPLICATE_RESOURCE", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex){
        Map<String, String> fieldErrors = new HashMap<>();

        for(FieldError error : ex.getBindingResult().getFieldErrors()){
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", "Request validation " +
                "failed", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex){
        ErrorResponse response = new ErrorResponse("NOT_FOUND", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ErrorResponse> handleChangeOfPasswordMismatch(InvalidCurrentPasswordException ex){
        ErrorResponse response = new ErrorResponse("PASSWORD_MISMATCH", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
