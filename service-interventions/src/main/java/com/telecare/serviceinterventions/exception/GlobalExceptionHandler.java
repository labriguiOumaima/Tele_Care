package com.telecare.serviceinterventions.exception;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<String> notFound(FeignException.NotFound ex) {
        return ResponseEntity.status(404).body("Ressource externe introuvable");
    }

    @ExceptionHandler(ServiceExterneIndisponibleException.class)
    public ResponseEntity<String> handleServiceExterneIndisponible(ServiceExterneIndisponibleException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> feign(FeignException ex) {
        log.error("FeignException status={} : {}", ex.status(), ex.getMessage(), ex);
        if (ex.status() == -1) {
            return ResponseEntity.status(503).body("Échec de communication avec un service");
        }
        if (ex.status() == 503) {
            return ResponseEntity.status(503).body("Service ou instance indisponible");
        }
        return ResponseEntity.status(502).body("Erreur HTTP du service distant : " + ex.status());
    }
}