package com.company.sales.infrastructure.adapters.out.stub;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.JudicialBackgroundPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class JudicialBackgroundStubAdapter implements JudicialBackgroundPort {

    private static final Logger log = LoggerFactory.getLogger(JudicialBackgroundStubAdapter.class);
    private static final double REJECTION_PROBABILITY = 0.20;

    @Override
    public ValidationResult checkBackground(Lead lead) {
        log.info("Starting judicial background check...");
        simulateLatency(600);
        log.info("Judicial background check completed.");

        if (ThreadLocalRandom.current().nextDouble() < REJECTION_PROBABILITY) {
            log.warn("Judicial background check failed for lead: records found.");
            return new ValidationResult(ValidationStatus.REJECTED, "Criminal records found.");
        }

        return new ValidationResult(ValidationStatus.APPROVED, "No criminal records found.");
    }

    private void simulateLatency(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}