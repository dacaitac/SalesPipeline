package com.company.sales.infrastructure.adapters.out.stub;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.NationalRegistryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NationalRegistryStubAdapter implements NationalRegistryPort {
    private static final Logger log = LoggerFactory.getLogger(NationalRegistryStubAdapter.class);
    @Override
    public ValidationResult validate(Lead lead) {
        simulateLatency(500);
        log.info("National Registry check completed.");
        return new ValidationResult(ValidationStatus.APPROVED, "Registry matched.");
    }
    private void simulateLatency(int ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}