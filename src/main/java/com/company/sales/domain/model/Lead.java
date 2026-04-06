package com.company.sales.domain.model;

import com.company.sales.domain.exception.DomainValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public record Lead(
        String nationalId,
        LocalDate dateOfBirth,
        String firstName,
        String lastName,
        String email,
        ValidationStatus validationStatus,
        int retryCount,
        LocalDateTime nextRetryTime
) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public Lead {
        if (nationalId == null || nationalId.trim().isEmpty()) {
            throw new DomainValidationException("National ID cannot be null or empty.");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new DomainValidationException("First name cannot be null or empty.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new DomainValidationException("Invalid email format.");
        }
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new DomainValidationException("Date of birth cannot be in the future.");
        }
    }

    public Lead withValidationStatus(ValidationStatus newStatus) {
        return new Lead(nationalId, dateOfBirth, firstName, lastName, email,
                newStatus, retryCount, nextRetryTime);
    }

    public Lead prepareNextAsyncRetry(int delayMinutes) {
        return new Lead(nationalId, dateOfBirth, firstName, lastName, email,
                ValidationStatus.PENDING, retryCount + 1,
                LocalDateTime.now().plusMinutes(delayMinutes));
    }

    public Lead markForManualReview() {
        return new Lead(nationalId, dateOfBirth, firstName, lastName, email,
                ValidationStatus.MANUAL_REVIEW, retryCount, null);
    }
}