package com.skillvault.backend.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionModel> handleResponseStatusException(ResponseStatusException e){
        return ResponseEntity.status(e.getStatusCode()).body(
                new ExceptionModel(
                        e.getStatusCode().value(),
                        e.getClass().getSimpleName(),
                        e.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ExceptionModel> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        ExceptionModel error = new ExceptionModel(
                HttpStatus.CONFLICT.value(),
                ex.getClass().getSimpleName(),
                "Database constraint violated. Please check your input."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
