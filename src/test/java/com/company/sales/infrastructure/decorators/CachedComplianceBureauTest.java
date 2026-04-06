package com.company.sales.infrastructure.decorators;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import com.company.sales.domain.ports.out.ComplianceCachePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachedComplianceBureauTest {

    @Mock private ComplianceBureauPort delegate;
    @Mock private ComplianceCachePort cachePort;

    private CachedComplianceBureau decorator;
    private Lead dummyLead;

    @BeforeEach
    void setUp() {
        decorator = new CachedComplianceBureau(delegate, cachePort);
        dummyLead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe", "test@test.com", ValidationStatus.PENDING, 0, null);
    }

    @Test
    void shouldReturnCachedResultWhenAvailable() {
        ValidationResult cachedResult = new ValidationResult(ValidationStatus.APPROVED, "Cached");
        when(cachePort.getCachedResult("123")).thenReturn(Optional.of(cachedResult));

        ValidationResult result = decorator.verifyCompliance(dummyLead);

        assertEquals(ValidationStatus.APPROVED, result.status());
        verify(delegate, never()).verifyCompliance(any());
    }

    @Test
    void shouldCallDelegateAndSaveWhenCacheMiss() {
        ValidationResult newResult = new ValidationResult(ValidationStatus.APPROVED, "Fresh");
        when(cachePort.getCachedResult("123")).thenReturn(Optional.empty());
        when(delegate.verifyCompliance(dummyLead)).thenReturn(newResult);

        ValidationResult result = decorator.verifyCompliance(dummyLead);

        assertEquals(ValidationStatus.APPROVED, result.status());
        verify(delegate).verifyCompliance(dummyLead);
        verify(cachePort).saveResult("123", newResult);
    }
}