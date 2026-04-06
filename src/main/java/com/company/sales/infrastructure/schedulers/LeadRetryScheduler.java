package com.company.sales.infrastructure.schedulers;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.ports.in.LeadOrchestrationUseCase;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class LeadRetryScheduler {
    private static final Logger log = LoggerFactory.getLogger(LeadRetryScheduler.class);
    
    private final LeadRepositoryPort leadRepositoryPort;
    private final LeadOrchestrationUseCase orchestrationUseCase;

    public LeadRetryScheduler(LeadRepositoryPort leadRepositoryPort, LeadOrchestrationUseCase orchestrationUseCase) {
        this.leadRepositoryPort = leadRepositoryPort;
        this.orchestrationUseCase = orchestrationUseCase;
    }

    /**
     * Executes every minute to find leads stuck in PENDING status due to previous failures.
     */
    @Scheduled(cron = "0 * * * * *")
    public void processPendingRetries() {
        LocalDateTime now = LocalDateTime.now();
        List<Lead> pendingLeads = leadRepositoryPort.findLeadsPendingForRetry(now);

        if (!pendingLeads.isEmpty()) {
            log.info("Scheduler found {} leads pending for retry. Re-injecting into pipeline...", pendingLeads.size());
            pendingLeads.forEach(lead -> {
                log.info("Re-processing lead: {}", lead.nationalId());
                orchestrationUseCase.processLead(lead);
            });
        }
    }
}