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
public class CachedComplianceBureau implements ComplianceBureauPort {
    private static final Logger log = LoggerFactory.getLogger(CachedComplianceBureau.class);
    private final ComplianceBureauPort bureauService;
    private final ComplianceCachePort cache;

    public CachedComplianceBureau(ComplianceBureauPort bureauService, ComplianceCachePort cache) {
        this.bureauService = bureauService;
        this.cache = cache;
    }

    @Override
    public ValidationResult verifyCompliance(Lead lead) {
        Optional<ValidationResult> cached = cache.getCachedResult(lead.nationalId());
        if (cached.isPresent()) {
            log.info("Compliance result retrieved from cache.");
            return cached.get();
        }
        
        ValidationResult result = bureauService.verifyCompliance(lead);
        if (result.isSuccessful()) {
            cache.saveResult(lead.nationalId(), result);
        }

        return result;
    }
}