package com.company.sales.domain.model;

import com.company.sales.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Domain Model: Lead Entity Tests")
class LeadTest {

    @Test
    @DisplayName("Should create a new Lead instance with updated ValidationStatus ensuring immutability")
    void shouldUpdateValidationStatusImmutably() {
        // Arrange
        Lead originalLead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe", 
                                     "john@test.com", ValidationStatus.PENDING, 0, null);

        // Act
        Lead updatedLead = originalLead.withValidationStatus(ValidationStatus.APPROVED);

        // Assert
        assertThat(updatedLead).isNotSameAs(originalLead);
        assertThat(updatedLead.validationStatus()).isEqualTo(ValidationStatus.APPROVED);
        assertThat(updatedLead.nationalId()).isEqualTo(originalLead.nationalId());
    }

    @Test
    @DisplayName("Should increment retry count and set next retry time when preparing for async retry")
    void shouldPrepareNextAsyncRetryCorrectly() {
        // Arrange
        Lead lead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe", 
                             "john@test.com", ValidationStatus.PENDING, 1, null);
        int delayMinutes = 5;

        // Act
        Lead retryLead = lead.prepareNextAsyncRetry(delayMinutes);

        // Assert
        assertThat(retryLead.retryCount()).isEqualTo(2);
        assertThat(retryLead.validationStatus()).isEqualTo(ValidationStatus.PENDING);
        assertThat(retryLead.nextRetryTime())
                .isNotNull()
                .isAfter(LocalDateTime.now().plusMinutes(delayMinutes - 1))
                .isBefore(LocalDateTime.now().plusMinutes(delayMinutes + 1));
    }

    @Test
    @DisplayName("Should reset retry time and set status to MANUAL_REVIEW when marking for manual review")
    void shouldMarkForManualReviewCorrectly() {
        // Arrange
        Lead lead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe", 
                             "john@test.com", ValidationStatus.PENDING, 3, LocalDateTime.now());

        // Act
        Lead manualReviewLead = lead.markForManualReview();

        // Assert
        assertThat(manualReviewLead.validationStatus()).isEqualTo(ValidationStatus.MANUAL_REVIEW);
        assertThat(manualReviewLead.retryCount()).isEqualTo(3);
        assertThat(manualReviewLead.nextRetryTime()).isNull();
    }

    @Test
    @DisplayName("Should successfully create a Lead when all parameters are valid")
    void shouldCreateLeadWhenValid() {
        // Arrange & Act
        Lead lead = new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe",
                "john.doe@test.com", ValidationStatus.PENDING, 0, null);

        // Assert
        assertThat(lead).isNotNull();
        assertThat(lead.nationalId()).isEqualTo("123");
    }

    @Test
    @DisplayName("Should throw DomainValidationException when National ID is empty")
    void shouldThrowExceptionWhenNationalIdIsEmpty() {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new Lead("", LocalDate.of(1990, 1, 1), "John", "Doe",
                "john@test.com", ValidationStatus.PENDING, 0, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("National ID cannot be null or empty.");
    }

    @Test
    @DisplayName("Should throw DomainValidationException when email format is invalid")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new Lead("123", LocalDate.of(1990, 1, 1), "John", "Doe",
                "invalid-email", ValidationStatus.PENDING, 0, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Invalid email format.");
    }

    @Test
    @DisplayName("Should throw DomainValidationException when date of birth is in the future")
    void shouldThrowExceptionWhenDateOfBirthIsFuture() {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new Lead("123", LocalDate.now().plusDays(1), "John", "Doe",
                "john@test.com", ValidationStatus.PENDING, 0, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Date of birth cannot be in the future.");
    }
}