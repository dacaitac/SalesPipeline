package com.company.sales.domain.model;

public record ValidationResult(ValidationStatus status, String reason) {
    public boolean isSuccessful() {
        return status == ValidationStatus.APPROVED;
    }
}