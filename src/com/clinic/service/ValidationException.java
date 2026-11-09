package com.clinic.service;

/**
 * Exception thrown when business rules, data integrity constraints,
 * or mandatory field validations are violated.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

