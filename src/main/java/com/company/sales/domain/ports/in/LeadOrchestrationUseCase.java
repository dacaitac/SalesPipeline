package com.company.sales.domain.ports.in;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;

import java.util.concurrent.CompletableFuture;

public interface LeadOrchestrationUseCase {
    CompletableFuture<ValidationResult> processLead(Lead lead);
}