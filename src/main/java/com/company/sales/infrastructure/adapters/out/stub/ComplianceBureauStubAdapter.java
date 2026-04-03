package com.company.sales.infrastructure.adapters.out.stub;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComplianceBureauStubAdapter implements ComplianceBureauPort {
    private static final Logger log = LoggerFactory.getLogger(ComplianceBureauStubAdapter.class);
    @Override
    public ValidationResult verifyCompliance(Lead lead) {
        log.info("Calling external Compliance Bureau...");
        // Simulate network failure randomly
        if (Math.random() > 0.7) {
            throw new RuntimeException("Connection reset by peer");
        }
        try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return new ValidationResult(ValidationStatus.APPROVED, "OFAC Cleared.");
    }
}