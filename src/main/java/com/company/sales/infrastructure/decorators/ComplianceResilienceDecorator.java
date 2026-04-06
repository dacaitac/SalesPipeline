package com.company.sales.infrastructure.decorators;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComplianceResilienceDecorator implements ComplianceBureauPort {
    private static final Logger log = LoggerFactory.getLogger(ComplianceResilienceDecorator.class);
    private static final int MAX_LOCAL_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final ComplianceBureauPort delegate;

    public ComplianceResilienceDecorator(ComplianceBureauPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public ValidationResult verifyCompliance(Lead lead) {
        int attempts = 0;
        while (attempts < MAX_LOCAL_RETRIES) {
            try {
                return delegate.verifyCompliance(lead);
            } catch (Exception e) {
                attempts++;
                log.warn("Local retry attempt {} for lead {}. Reason: {}", attempts, lead.nationalId(), e.getMessage());
                if (attempts >= MAX_LOCAL_RETRIES) {
                    log.error("Local resilience exhausted for lead {}", lead.nationalId());
                    throw e; // Escalamos al orquestador para reintento asíncrono
                }
                waitForNextAttempt();
            }
        }
        throw new RuntimeException("Unreachable state in Resilience Decorator");
    }

    private void waitForNextAttempt() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}