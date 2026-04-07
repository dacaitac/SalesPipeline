package com.company.sales.infrastructure.schedulers;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.in.LeadOrchestrationUseCase;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import com.company.sales.infrastructure.adapters.in.LeadCliController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "app.repository.type=h2")
@Transactional
class LeadRetrySchedulerIT {

    @MockitoBean private LeadCliController cliController;
    @MockitoBean private LeadOrchestrationUseCase orchestrationUseCase;

    @Autowired private LeadRepositoryPort leadRepository;
    @Autowired private LeadRetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        leadRepository.findAll().forEach(lead -> leadRepository.deleteById(lead.nationalId()));
    }

    @Test
    void shouldProcessOnlyExpiredPendingLeads() {
        // Arrange
        Lead validPendingLead = new Lead("VALID-123", LocalDate.of(1990, 1, 1), "John", "Valid",
                "valid@test.com", ValidationStatus.PENDING, 1, LocalDateTime.now().minusMinutes(5));

        Lead invalidFutureLead = new Lead("INVALID-456", LocalDate.of(1990, 1, 1), "Jane", "Future",
                "future@test.com", ValidationStatus.PENDING, 1, LocalDateTime.now().plusMinutes(10));

        leadRepository.save(validPendingLead);
        leadRepository.save(invalidFutureLead);

        // Act
        scheduler.processPendingRetries();

        // Assert
        verify(orchestrationUseCase).processLead(argThat(lead -> lead.nationalId().equals("VALID-123")));
        verify(orchestrationUseCase, never()).processLead(argThat(lead -> lead.nationalId().equals("INVALID-456")));
    }
}