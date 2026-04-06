package com.company.sales.application.service;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Application Service: Lead Orchestrator Tests")
class LeadOrchestratorTest {

    @Mock private NationalRegistryPort registryPort;
    @Mock private JudicialBackgroundPort judicialPort;
    @Mock private ComplianceBureauPort compliancePort;
    @Mock private QualificationScorerPort scorerPort;
    @Mock private LeadRepositoryPort leadRepositoryPort;

    private ExecutorService executorService;
    private LeadOrchestrator orchestrator;
    private Lead dummyLead;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(2);
        orchestrator = new LeadOrchestrator(registryPort, judicialPort, compliancePort,
                scorerPort, leadRepositoryPort, executorService);
        dummyLead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe",
                "john@test.com", ValidationStatus.PENDING, 0, null);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    @DisplayName("Should approve lead and persist APPROVED status when all parallel and sequential checks pass")
    void shouldApproveLeadWhenAllChecksPass() {
        // Arrange
        when(registryPort.validate(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(compliancePort.verifyCompliance(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(scorerPort.calculateScore(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "Score OK"));

        // Act
        ValidationResult result = orchestrator.processLead(dummyLead).join();

        // Assert
        assertThat(result.status()).isEqualTo(ValidationStatus.APPROVED);

        ArgumentCaptor<Lead> leadCaptor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepositoryPort, timeout(1000)).save(leadCaptor.capture());
        assertThat(leadCaptor.getValue().validationStatus()).isEqualTo(ValidationStatus.APPROVED);
    }

    @Test
    @DisplayName("Should return REJECTED status and halt pipeline when registry validation fails")
    void shouldRejectWhenRegistryFails() {
        // Arrange
        when(registryPort.validate(any())).thenReturn(new ValidationResult(ValidationStatus.REJECTED, "Registry fail"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));

        // Act
        ValidationResult result = orchestrator.processLead(dummyLead).join();

        // Assert
        assertThat(result.status()).isEqualTo(ValidationStatus.REJECTED);
        verify(compliancePort, never()).verifyCompliance(any());

        ArgumentCaptor<Lead> leadCaptor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepositoryPort, timeout(1000)).save(leadCaptor.capture());
        assertThat(leadCaptor.getValue().validationStatus()).isEqualTo(ValidationStatus.REJECTED);
    }

    @Test
    @DisplayName("Should schedule lead for async retry when an external service throws a runtime exception")
    void shouldScheduleRetryOnPipelineException() {
        // Arrange
        when(registryPort.validate(any())).thenThrow(new RuntimeException("Simulated timeout"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));

        // Act
        ValidationResult result = orchestrator.processLead(dummyLead).join();

        // Assert
        assertThat(result.status()).isEqualTo(ValidationStatus.PENDING);
        assertThat(result.reason()).contains("System error, retry scheduled");

        ArgumentCaptor<Lead> leadCaptor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepositoryPort, timeout(1000)).save(leadCaptor.capture());

        Lead savedLead = leadCaptor.getValue();
        assertThat(savedLead.validationStatus()).isEqualTo(ValidationStatus.PENDING);
        assertThat(savedLead.retryCount()).isEqualTo(1);
        assertThat(savedLead.nextRetryTime()).isNotNull();
    }
}