package com.winwin.auth.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    record ErrorResponse(String error) {
    }

    @ExceptionHandler({DuplicateEmailException.class, DataIntegrityViolationException.class})
    ResponseEntity<ErrorResponse> duplicateEmail() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Email is already registered"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> invalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid email or password"));
    }

    @ExceptionHandler(DataApiUnavailableException.class)
    ResponseEntity<ErrorResponse> dataApiUnavailable(DataApiUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> invalidRequest() {
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request"));
    }
}
