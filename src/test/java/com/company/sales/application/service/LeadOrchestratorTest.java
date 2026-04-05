package com.company.sales.application.service;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import com.company.sales.domain.ports.out.JudicialBackgroundPort;
import com.company.sales.domain.ports.out.NationalRegistryPort;
import com.company.sales.domain.ports.out.QualificationScorerPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private ExecutorService executorService;
    private LeadOrchestrator orchestrator;
    private Lead dummyLead;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(2);
        orchestrator = new LeadOrchestrator(registryPort, judicialPort, compliancePort, scorerPort, executorService);
        dummyLead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe", "john@test.com");
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void shouldApproveLeadWhenAllChecksPass() {
        when(registryPort.validate(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(compliancePort.verifyCompliance(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(scorerPort.calculateScore(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "Score OK"));

        ValidationResult result = orchestrator.processLead(dummyLead).join();

        assertEquals(ValidationStatus.APPROVED, result.status());
        verify(registryPort).validate(dummyLead);
        verify(judicialPort).checkBackground(dummyLead);
        verify(compliancePort).verifyCompliance(dummyLead);
        verify(scorerPort).calculateScore(dummyLead);
    }

    @Test
    void shouldRejectWhenRegistryFails() {
        when(registryPort.validate(any())).thenReturn(new ValidationResult(ValidationStatus.REJECTED, "Registry fail"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));

        ValidationResult result = orchestrator.processLead(dummyLead).join();

        assertEquals(ValidationStatus.REJECTED, result.status());
        verify(compliancePort, never()).verifyCompliance(any());
    }

    @Test
    void shouldRejectWhenComplianceFails() {
        when(registryPort.validate(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(compliancePort.verifyCompliance(any())).thenReturn(new ValidationResult(ValidationStatus.REJECTED, "OFAC fail"));

        ValidationResult result = orchestrator.processLead(dummyLead).join();

        assertEquals(ValidationStatus.REJECTED, result.status());
        verify(scorerPort, never()).calculateScore(any());
    }

    @Test
    void shouldRejectWhenScorerFails() {
        when(registryPort.validate(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(compliancePort.verifyCompliance(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(scorerPort.calculateScore(any())).thenReturn(new ValidationResult(ValidationStatus.REJECTED, "Low Score"));

        ValidationResult result = orchestrator.processLead(dummyLead).join();

        assertEquals(ValidationStatus.REJECTED, result.status());
    }
}