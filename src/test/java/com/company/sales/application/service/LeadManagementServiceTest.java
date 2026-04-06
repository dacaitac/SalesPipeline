package com.company.sales.application.service;

import com.company.sales.domain.exception.ResourceNotFoundException;
import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Application Service: Lead Management Service Tests")
class LeadManagementServiceTest {

    @Mock
    private LeadRepositoryPort leadRepositoryPort;

    @InjectMocks
    private LeadManagementService service;

    private Lead dummyLead;

    @BeforeEach
    void setUp() {
        dummyLead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe",
                "john@test.com", ValidationStatus.PENDING, 0, null);
    }

    @Test
    @DisplayName("Should update lead successfully when it exists in repository")
    void shouldUpdateLeadWhenExists() {
        // Arrange
        when(leadRepositoryPort.findById("123")).thenReturn(Optional.of(dummyLead));
        when(leadRepositoryPort.save(any(Lead.class))).thenReturn(dummyLead);

        // Act
        Lead updated = service.updateLead(dummyLead);

        // Assert
        assertThat(updated).isNotNull();
        verify(leadRepositoryPort).findById("123");
        verify(leadRepositoryPort).save(dummyLead);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating a non-existent lead")
    void shouldThrowExceptionWhenUpdatingNonExistentLead() {
        // Arrange
        when(leadRepositoryPort.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.updateLead(dummyLead))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lead with National ID 123 not found.");
        
        verify(leadRepositoryPort, never()).save(any());
    }
}