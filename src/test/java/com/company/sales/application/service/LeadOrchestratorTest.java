package com.company.sales.application.service;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import com.company.sales.domain.ports.out.JudicialBackgroundPort;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import com.company.sales.domain.ports.out.NationalRegistryPort;
import com.company.sales.domain.ports.out.QualificationScorerPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        orchestrator = new LeadOrchestrator(registryPort, judicialPort, compliancePort, scorerPort, leadRepositoryPort, executorService);
        dummyLead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe", "john@test.com", ValidationStatus.PENDING, 0, null);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void shouldApproveLeadAndSaveStatusWhenAllChecksPass() {
        when(registryPort.validate(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(compliancePort.verifyCompliance(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(scorerPort.calculateScore(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "Score OK"));

        ValidationResult result = orchestrator.processLead(dummyLead).join();

        assertEquals(ValidationStatus.APPROVED, result.status());

        ArgumentCaptor<Lead> leadCaptor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepositoryPort, timeout(1000)).save(leadCaptor.capture());
        assertEquals(ValidationStatus.APPROVED, leadCaptor.getValue().validationStatus());
    }

    @Test
    void shouldRejectAndSaveStatusWhenRegistryFails() {
        when(registryPort.validate(any())).thenReturn(new ValidationResult(ValidationStatus.REJECTED, "Registry fail"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));

        ValidationResult result = orchestrator.processLead(dummyLead).join();

        assertEquals(ValidationStatus.REJECTED, result.status());
        verify(compliancePort, never()).verifyCompliance(any());

        ArgumentCaptor<Lead> leadCaptor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepositoryPort, timeout(1000)).save(leadCaptor.capture());
        assertEquals(ValidationStatus.REJECTED, leadCaptor.getValue().validationStatus());
    }
}