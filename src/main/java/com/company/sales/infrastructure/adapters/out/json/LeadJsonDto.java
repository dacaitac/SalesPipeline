package com.company.sales.infrastructure.adapters.out.json;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeadJsonDto {
    public String nationalId;
    public LocalDate dateOfBirth;
    public String firstName;
    public String lastName;
    public String email;
    public String validationStatus;
    public int retryCount;
    public LocalDateTime nextRetryTime;

    public LeadJsonDto() {}
}