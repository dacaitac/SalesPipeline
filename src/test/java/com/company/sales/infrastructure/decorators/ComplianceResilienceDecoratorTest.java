package com.company.sales.infrastructure.decorators;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("Infrastructure: Compliance Resilience Decorator Tests")
class ComplianceResilienceDecoratorTest {

    @Test
    @DisplayName("Should throw exception and exhaust local retries when underlying service fails continuously")
    void shouldThrowExceptionAfterMaxRetries() {
        // Arrange
        ComplianceBureauPort delegate = mock(ComplianceBureauPort.class);
        Lead dummyLead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe",
                "test@test.com", ValidationStatus.PENDING, 0, null);

        when(delegate.verifyCompliance(dummyLead)).thenThrow(new RuntimeException("Network Error"));
        ComplianceResilienceDecorator decorator = new ComplianceResilienceDecorator(delegate);

        // Act & Assert
        assertThatThrownBy(() -> decorator.verifyCompliance(dummyLead))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Network Error");

        // Verify that it attempted exactly 3 times (MAX_LOCAL_RETRIES) before propagating the error
        verify(delegate, times(3)).verifyCompliance(dummyLead);
    }
}