package com.company.sales.application.service;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.*;
import com.company.sales.infrastructure.adapters.in.LeadCliController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "app.repository.type=h2")
class LeadOrchestratorIT {

    @MockitoBean private LeadCliController cliController;
    @MockitoBean private NationalRegistryPort registryPort;
    @MockitoBean private JudicialBackgroundPort judicialPort;

    @MockitoBean private ComplianceBureauPort compliancePort;
    @MockitoBean private QualificationScorerPort scorerPort;

    @Autowired private LeadOrchestrator orchestrator;
    @Autowired private LeadRepositoryPort leadRepository;

    private Lead testLead;

    @BeforeEach
    void setUp() {
        testLead = new Lead("12345", LocalDate.of(1990, 1, 1), "John", "Doe",
                "john.doe@test.com", ValidationStatus.PENDING, 0, null);
        leadRepository.save(testLead);
    }

    @Test
    void shouldPersistApprovedStatusWhenAllPortsSucceed() {
        // Arrange
        when(registryPort.validate(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(compliancePort.verifyCompliance(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));
        when(scorerPort.calculateScore(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));

        // Act
        orchestrator.processLead(testLead).join();

        // Assert
        Optional<Lead> persistedLead = leadRepository.findById("12345");
        assertThat(persistedLead).isPresent();
        assertThat(persistedLead.get().validationStatus()).isEqualTo(ValidationStatus.APPROVED);
    }

    @Test
    void shouldPersistPendingStatusWithRetryWhenPortThrowsException() {
        // Arrange
        when(registryPort.validate(any())).thenThrow(new RuntimeException("Simulated External Failure"));
        when(judicialPort.checkBackground(any())).thenReturn(new ValidationResult(ValidationStatus.APPROVED, "OK"));

        // Act
        orchestrator.processLead(testLead).join();

        // Assert
        Optional<Lead> persistedLead = leadRepository.findById("12345");
        assertThat(persistedLead).isPresent();
        Lead dbLead = persistedLead.get();
        assertThat(dbLead.validationStatus()).isEqualTo(ValidationStatus.PENDING);
        assertThat(dbLead.retryCount()).isEqualTo(1);
        assertThat(dbLead.nextRetryTime()).isNotNull();
    }
}