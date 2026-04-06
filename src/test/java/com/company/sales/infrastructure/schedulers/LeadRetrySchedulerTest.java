package com.company.sales.infrastructure.schedulers;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.in.LeadOrchestrationUseCase;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Infrastructure: Lead Retry Scheduler Tests")
class LeadRetrySchedulerTest {

    @Mock
    private LeadRepositoryPort leadRepositoryPort;

    @Mock
    private LeadOrchestrationUseCase orchestrationUseCase;

    @InjectMocks
    private LeadRetryScheduler scheduler;

    @Test
    @DisplayName("Should inject leads into orchestration pipeline when pending retries are found")
    void shouldProcessPendingRetriesWhenLeadsExist() {
        // Arrange
        Lead pendingLead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe", 
                                    "test@test.com", ValidationStatus.PENDING, 1, LocalDateTime.now().minusMinutes(1));
        
        when(leadRepositoryPort.findLeadsPendingForRetry(any(LocalDateTime.class)))
                .thenReturn(List.of(pendingLead));

        // Act
        scheduler.processPendingRetries();

        // Assert
        verify(orchestrationUseCase, times(1)).processLead(pendingLead);
    }

    @Test
    @DisplayName("Should remain idle and not trigger orchestration when no pending leads are found")
    void shouldNotTriggerOrchestrationWhenNoLeadsPending() {
        // Arrange
        when(leadRepositoryPort.findLeadsPendingForRetry(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        scheduler.processPendingRetries();

        // Assert
        verify(orchestrationUseCase, never()).processLead(any(Lead.class));
    }
}