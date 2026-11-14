package com.telecare.serviceinterventions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceExterneIndisponibleException extends RuntimeException {
    public ServiceExterneIndisponibleException(String message) {
        super(message);
    }
}