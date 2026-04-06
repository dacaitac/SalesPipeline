package com.company.sales.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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