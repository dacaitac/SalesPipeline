package com.company.sales.infrastructure.decorators;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import com.company.sales.domain.ports.out.ComplianceCachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Decorator to add caching logic to Compliance Bureau calls.
 */
public class ComplianceCachingDecorator implements ComplianceBureauPort {
    private static final Logger log = LoggerFactory.getLogger(ComplianceCachingDecorator.class);
    private final ComplianceBureauPort delegate;
    private final ComplianceCachePort cachePort;

    public ComplianceCachingDecorator(ComplianceBureauPort delegate, ComplianceCachePort cachePort) {
        this.delegate = delegate;
        this.cachePort = cachePort;
    }

    @Override
    public ValidationResult verifyCompliance(Lead lead) {
        Optional<ValidationResult> cached = cachePort.getCachedResult(lead.nationalId());
        if (cached.isPresent()) {
            log.info("Compliance result retrieved from cache.");
            return cached.get();
        }
        
        ValidationResult result = delegate.verifyCompliance(lead);
        if (result.isSuccessful()) {
            cachePort.saveResult(lead.nationalId(), result);
        }
        return result;
    }
}