package com.company.sales.infrastructure.adapters.out.stub;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class ComplianceBureauStubAdapter implements ComplianceBureauPort {

    private static final Logger log = LoggerFactory.getLogger(ComplianceBureauStubAdapter.class);
    private static final double FAILURE_PROBABILITY = 0.75;
    private static final int SIMULATED_LATENCY_MS = 800;

    @Override
    public ValidationResult verifyCompliance(Lead lead) {
        log.info("Calling external Compliance Bureau...");

        simulateFailure();
        simulateLatency();

        return new ValidationResult(
                ValidationStatus.APPROVED,
                "OFAC Cleared."
        );
    }

    private void simulateFailure() {
        double random = ThreadLocalRandom.current().nextDouble();
        if (random < FAILURE_PROBABILITY) {
            log.warn("Simulated external service failure");
            throw new RuntimeException("Connection reset by peer");
        }
    }

    private void simulateLatency() {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}