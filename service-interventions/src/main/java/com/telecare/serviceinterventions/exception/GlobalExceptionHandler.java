package com.telecare.serviceinterventions.exception;

import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<String> notFound(FeignException.NotFound ex) {
        return ResponseEntity.status(404).body("Ressource externe introuvable");
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> feign(FeignException ex) {
        if (ex.status() == -1) {
            return ResponseEntity.status(503).body("Échec de communication avec un service");
        }
        if (ex.status() == 503) {
            return ResponseEntity.status(503).body("Service ou instance indisponible");
        }
        return ResponseEntity.status(502).body("Erreur HTTP du service distant : " + ex.status());
    }
}