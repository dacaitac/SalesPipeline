package com.company.sales.domain.exception;

/**
 * Exception thrown when domain validation rules are violated during the instantiation
 * or manipulation of domain models.
 */
public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String message) {
        super(message);
    }
}