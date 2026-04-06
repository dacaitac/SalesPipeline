package com.company.sales.domain.exception;

/**
 * Exception thrown when a requested resource (e.g., a Lead) cannot be found
 * in the underlying data repository.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}