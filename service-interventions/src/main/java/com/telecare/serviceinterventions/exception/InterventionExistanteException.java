package com.telecare.serviceinterventions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InterventionExistanteException extends RuntimeException {
    public InterventionExistanteException(Long alertId) {
        super("Une intervention existe deja pour l'alerte " + alertId);
    }
}