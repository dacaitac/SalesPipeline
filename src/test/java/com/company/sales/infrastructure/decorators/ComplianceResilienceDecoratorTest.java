package com.company.sales.infrastructure.decorators;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComplianceResilienceDecoratorTest {

    @Test
    void shouldReturnManualReviewOnException() {
        ComplianceBureauPort delegate = mock(ComplianceBureauPort.class);
        Lead dummyLead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe", "test@test.com");
        
        when(delegate.verifyCompliance(dummyLead)).thenThrow(new RuntimeException("Network Error"));
        
        ComplianceResilienceDecorator decorator = new ComplianceResilienceDecorator(delegate);
        ValidationResult result = decorator.verifyCompliance(dummyLead);

        assertEquals(ValidationStatus.MANUAL_REVIEW, result.status());
        assertEquals("Service unavailable, manual review required.", result.reason());
    }
}