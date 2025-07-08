package com.skillvault.backend.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
                        e.getReason(),e.getMessage()) );
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
                "Database constraint violated. Please check your input.",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }



    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionModel> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex){
        ExceptionModel error = new ExceptionModel(
                HttpStatus.BAD_REQUEST.value(),
                ex.getClass().getSimpleName(),
                "Your request URL is invalid, check if you have typed the UUID correctly",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionModel> handleGenericException(Exception ex){
        ExceptionModel error = new ExceptionModel(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getClass().getSimpleName(),
                "An Internal server error occurred, contact DEV team to resolve it.",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
