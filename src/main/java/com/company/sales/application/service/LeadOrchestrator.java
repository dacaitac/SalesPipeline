package com.company.sales.application.service;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.in.LeadOrchestrationUseCase;
import com.company.sales.domain.ports.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class LeadOrchestrator implements LeadOrchestrationUseCase {

    private static final Logger log = LoggerFactory.getLogger(LeadOrchestrator.class);
    private static final int TIMEOUT_SECONDS = 5;
    private static final int MAX_ASYNC_RETRIES = 3;

    private final NationalRegistryPort registryPort;
    private final JudicialBackgroundPort judicialPort;
    private final ComplianceBureauPort compliancePort;
    private final QualificationScorerPort scorerPort;
    private final LeadRepositoryPort leadRepositoryPort;
    private final ExecutorService executor;

    public LeadOrchestrator(NationalRegistryPort registryPort, JudicialBackgroundPort judicialPort,
                            ComplianceBureauPort compliancePort, QualificationScorerPort scorerPort,
                            LeadRepositoryPort leadRepositoryPort, ExecutorService executor) {
        this.registryPort = registryPort;
        this.judicialPort = judicialPort;
        this.compliancePort = compliancePort;
        this.scorerPort = scorerPort;
        this.leadRepositoryPort = leadRepositoryPort;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<ValidationResult> processLead(Lead lead) {
        MDC.put("leadId", lead.nationalId());
        log.info("Processing pipeline for lead. Attempt: {}", lead.retryCount());
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return executeParallelChecks(lead, contextMap)
                .thenApplyAsync(res -> executeSequentialCheck(res, () -> compliancePort.verifyCompliance(lead), contextMap), executor)
                .thenApplyAsync(res -> executeSequentialCheck(res, () -> scorerPort.calculateScore(lead), contextMap), executor)
                .thenApplyAsync(res -> finalizePipeline(lead, res, contextMap), executor)
                .exceptionally(ex -> handleError(lead, ex, contextMap))
                .whenComplete((res, ex) -> MDC.clear());
    }

    private CompletableFuture<ValidationResult> executeParallelChecks(Lead lead, Map<String, String> contextMap) {
        CompletableFuture<ValidationResult> registryCheck = CompletableFuture
                .supplyAsync(() -> executeWithMdc(contextMap, () -> registryPort.validate(lead)), executor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        CompletableFuture<ValidationResult> judicialCheck = CompletableFuture
                .supplyAsync(() -> executeWithMdc(contextMap, () -> judicialPort.checkBackground(lead)), executor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        return registryCheck.thenCombineAsync(judicialCheck,
                (reg, jud) -> reg.isSuccessful() && jud.isSuccessful() ? reg : (!reg.isSuccessful() ? reg : jud),
                executor);
    }

    private ValidationResult executeSequentialCheck(ValidationResult previousResult, Supplier<ValidationResult> nextCheck, Map<String, String> contextMap) {
        return executeWithMdc(contextMap, () -> previousResult.isSuccessful() ? nextCheck.get() : previousResult);
    }

    private ValidationResult finalizePipeline(Lead lead, ValidationResult result, Map<String, String> contextMap) {
        restoreMdc(contextMap);
        Lead finalLead = lead.withValidationStatus(result.status());
        leadRepositoryPort.save(finalLead);
        return result;
    }

    private ValidationResult handleError(Lead lead, Throwable ex, Map<String, String> contextMap) {
        restoreMdc(contextMap);
        log.error("Pipeline failed for lead {}: {}", lead.nationalId(), ex.getMessage());

        if (lead.retryCount() < MAX_ASYNC_RETRIES) {
            int delay = lead.retryCount() + 1;
            Lead retryLead = lead.prepareNextAsyncRetry(delay);
            leadRepositoryPort.save(retryLead);
            log.info("Lead {} scheduled for async retry in {} minutes", lead.nationalId(), delay);
        } else {
            leadRepositoryPort.save(lead.markForManualReview());
            log.warn("Max async retries reached for lead {}. Moved to MANUAL_REVIEW", lead.nationalId());
        }
        return new ValidationResult(ValidationStatus.PENDING, "System error, retry scheduled.");
    }

    private <T> T executeWithMdc(Map<String, String> contextMap, Supplier<T> action) {
        restoreMdc(contextMap);
        try {
            return action.get();
        } finally {
            MDC.clear();
        }
    }

    private void restoreMdc(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }
}