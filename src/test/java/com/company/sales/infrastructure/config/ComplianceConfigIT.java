package com.company.sales.infrastructure.config;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceBureauPort;
import com.company.sales.domain.ports.out.ComplianceCachePort;
import com.company.sales.infrastructure.adapters.in.LeadCliController;
import com.company.sales.infrastructure.decorators.CachedComplianceBureau;
import com.company.sales.infrastructure.decorators.RetryingComplianceBureau;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "app.repository.type=h2")
@Transactional
class ComplianceConfigIT {

    @MockitoBean private LeadCliController cliController;

    @Autowired private ComplianceBureauPort configuredPort;
    @Autowired private ComplianceCachePort cachePort;

    @Test
    void shouldWireDecoratorPatternCorrectly() {
        // Assert
        assertThat(configuredPort).isInstanceOf(RetryingComplianceBureau.class);

        Object innerCachedBureau = ReflectionTestUtils.getField(configuredPort, "bureauService");
        assertThat(innerCachedBureau).isInstanceOf(CachedComplianceBureau.class);
    }

    @Test
    void shouldWritePhysicalRecordToDatabaseOnSuccessfulValidation() {
        // Arrange
        Lead testLead = new Lead("99999", LocalDate.of(1990, 1, 1), "Jane", "Doe",
                "jane.doe@test.com", ValidationStatus.PENDING, 0, null);

        // Act
        ValidationResult result = null;
        int maxAttempts = 15;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                result = configuredPort.verifyCompliance(testLead);
                if (result.isSuccessful()) break;
            } catch (Exception ignored) {
                // Ignore the 75% simulated network failure from the stub to ensure test stability
            }
        }

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();

        Optional<ValidationResult> cachedResult = cachePort.getCachedResult("99999");
        assertThat(cachedResult).isPresent();
        assertThat(cachedResult.get().status()).isEqualTo(ValidationStatus.APPROVED);
    }
}