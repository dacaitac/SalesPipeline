package com.company.sales.infrastructure.adapters.out.stub;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.NationalRegistryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class NationalRegistryStubAdapter implements NationalRegistryPort {

    private static final Logger log = LoggerFactory.getLogger(NationalRegistryStubAdapter.class);
    private static final double REJECTION_PROBABILITY = 0.15;

    @Override
    public ValidationResult validate(Lead lead) {
        log.info("Starting National Registry check...");
        simulateLatency(500);
        log.info("National Registry check completed.");

        if (ThreadLocalRandom.current().nextDouble() < REJECTION_PROBABILITY) {
            log.warn("National Registry check failed for lead: ID not found or mismatched.");
            return new ValidationResult(ValidationStatus.REJECTED, "Registry match failed.");
        }

        return new ValidationResult(ValidationStatus.APPROVED, "Registry matched.");
    }

    private void simulateLatency(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}