package com.company.sales.infrastructure.decorators;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator to provide resilience (Fallback to MANUAL_REVIEW) on network failure.
 */
public class ComplianceResilienceDecorator implements ComplianceBureauPort {
    private static final Logger log = LoggerFactory.getLogger(ComplianceResilienceDecorator.class);
    private final ComplianceBureauPort delegate;

    public ComplianceResilienceDecorator(ComplianceBureauPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public ValidationResult verifyCompliance(Lead lead) {
        try {
            return delegate.verifyCompliance(lead);
        } catch (Exception e) {
            log.warn("Compliance Bureau service failed. Fallback triggered. Reason: {}", e.getMessage());
            return new ValidationResult(ValidationStatus.MANUAL_REVIEW, "Service unavailable, manual review required.");
        }
    }
}