package com.example.atol_integration_service.exceptions;

public class ValidationException extends RuntimeException {
    public ValidationException(final String message) {
        super(message);
    }
}