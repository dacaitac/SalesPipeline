package com.company.sales.application.service;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.in.LeadOrchestrationUseCase;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import com.company.sales.domain.ports.out.JudicialBackgroundPort;
import com.company.sales.domain.ports.out.NationalRegistryPort;
import com.company.sales.domain.ports.out.QualificationScorerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;

/**
 * Orchestrates the validation flow using structured concurrency.
 */
public class LeadOrchestrator implements LeadOrchestrationUseCase {

    private static final Logger log = LoggerFactory.getLogger(LeadOrchestrator.class);
    private static final int TIMEOUT_SECONDS = 5;

    private final NationalRegistryPort registryPort;
    private final JudicialBackgroundPort judicialPort;
    private final ComplianceBureauPort compliancePort;
    private final QualificationScorerPort scorerPort;
    private final ExecutorService executor;

    public LeadOrchestrator(
            NationalRegistryPort registryPort,
            JudicialBackgroundPort judicialPort,
            ComplianceBureauPort compliancePort,
            QualificationScorerPort scorerPort,
            ExecutorService executor) {
        this.registryPort = registryPort;
        this.judicialPort = judicialPort;
        this.compliancePort = compliancePort;
        this.scorerPort = scorerPort;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<ValidationResult> processLead(Lead lead) {
        MDC.put("leadId", lead.nationalId());
        log.info("Starting validation orchestration for lead.");
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        // Step 1: Parallel Execution
        CompletableFuture<ValidationResult> registryFuture = CompletableFuture.supplyAsync(
                        () -> executeWithMdc(contextMap, () -> registryPort.validate(lead)), executor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        CompletableFuture<ValidationResult> judicialFuture = CompletableFuture.supplyAsync(
                        () -> executeWithMdc(contextMap, () -> judicialPort.checkBackground(lead)), executor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        return registryFuture.thenCombineAsync(judicialFuture, (regResult, judResult) -> {
                    setMdc(contextMap);
                    if (!regResult.isSuccessful()) return regResult;
                    if (!judResult.isSuccessful()) return judResult;
                    log.info("Step 1 (Parallel) completed successfully.");
                    return new ValidationResult(ValidationStatus.APPROVED, "Parallel checks passed");
                }, executor)

                // Step 2: Sequential Execution - Compliance Bureau
                .thenApplyAsync(prevResult -> {
                    setMdc(contextMap);
                    if (!prevResult.isSuccessful()) return prevResult;
                    log.info("Starting Step 2: Compliance Bureau.");
                    return compliancePort.verifyCompliance(lead);
                }, executor)

                // Step 3: Sequential Execution - Qualification Scorer
                .thenApplyAsync(prevResult -> {
                    setMdc(contextMap);
                    if (!prevResult.isSuccessful()) return prevResult;
                    log.info("Starting Step 3: Qualification Scorer.");
                    return scorerPort.calculateScore(lead);
                }, executor)

                // Global Exception Handling
                .exceptionally(ex -> {
                    setMdc(contextMap);
                    log.error("Pipeline failed due to exception: {}", ex.getMessage());
                    return new ValidationResult(ValidationStatus.REJECTED, "System error during orchestration.");
                }).whenComplete((res, ex) -> MDC.clear());
    }

    private ValidationResult executeWithMdc(Map<String, String> contextMap, java.util.function.Supplier<ValidationResult> action) {
        setMdc(contextMap);
        try {
            return action.get();
        } finally {
            MDC.clear();
        }
    }

    private void setMdc(Map<String, String> contextMap) {
        if (contextMap != null) MDC.setContextMap(contextMap);
    }
}