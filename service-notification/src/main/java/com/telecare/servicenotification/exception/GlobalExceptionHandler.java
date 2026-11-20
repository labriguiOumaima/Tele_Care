package com.telecare.servicenotification.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<String> introuvable(RessourceIntrouvableException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}