package com.company.sales.domain.ports.out;

import com.company.sales.domain.model.ValidationResult;

import java.util.Optional;

public interface ComplianceCachePort {
    Optional<ValidationResult> getCachedResult(String nationalId);
    void saveResult(String nationalId, ValidationResult result);
}