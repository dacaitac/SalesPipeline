package com.company.sales.infrastructure.decorators;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryingComplianceBureau implements ComplianceBureauPort {
    private static final Logger log = LoggerFactory.getLogger(RetryingComplianceBureau.class);
    private static final int MAX_LOCAL_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final ComplianceBureauPort bureauService;

    public RetryingComplianceBureau(ComplianceBureauPort bureauService) {
        this.bureauService = bureauService;
    }

    @Override
    public ValidationResult verifyCompliance(Lead lead) {
        int attempts = 0;
        while (attempts < MAX_LOCAL_RETRIES) {
            try {
                return bureauService.verifyCompliance(lead);
            } catch (Exception e) {
                attempts++;
                log.warn("Local retry attempt {} for lead {}. Reason: {}", attempts, lead.nationalId(), e.getMessage());
                if (attempts >= MAX_LOCAL_RETRIES) {
                    log.error("Local resilience exhausted for lead {}", lead.nationalId());
                    throw e;
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