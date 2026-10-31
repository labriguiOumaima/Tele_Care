package com.telecare.serviceinterventions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TransitionInterditeException extends RuntimeException {
    public TransitionInterditeException(String message) {
        super(message);
    }
}