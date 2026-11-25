package com.telecare.utilisateurs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour service-utilisateurs.
 * Jalon : gestion d'erreurs structurée et cohérente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // DTO interne pour les réponses d'erreur
    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {}

    /**
     * Email déjà existant ou règle métier violée.
     */
    @ExceptionHandler(UtilisateurDejaExistantException.class)
    public ResponseEntity<ErrorResponse> handleDejaExistant(UtilisateurDejaExistantException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "CONFLIT",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    /**
     * Utilisateur introuvable (lever par le service ou par ResponseStatusException).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(
                        ex.getStatusCode().value(),
                        ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString(),
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    /**
     * Erreurs de validation Bean (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            erreurs.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "VALIDATION");
        body.put("champs", erreurs);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Toute autre exception non gérée.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "ERREUR_INTERNE",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }
}
