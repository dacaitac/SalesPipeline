package com.company.sales.infrastructure.adapters.out.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
public class LeadEntity {
    @Id
    private String nationalId;
    private LocalDate dateOfBirth;
    private String firstName;
    private String lastName;
    private String email;
    private String validationStatus;
    private int retryCount;
    private LocalDateTime nextRetryTime;

    protected LeadEntity() {}

    public LeadEntity(String nationalId, LocalDate dateOfBirth, String firstName, String lastName,
                      String email, String validationStatus, int retryCount, LocalDateTime nextRetryTime) {
        this.nationalId = nationalId;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.validationStatus = validationStatus;
        this.retryCount = retryCount;
        this.nextRetryTime = nextRetryTime;
    }

    public String getNationalId() { return nationalId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getValidationStatus() { return validationStatus; }
    public int getRetryCount() { return retryCount; }
    public LocalDateTime getNextRetryTime() { return nextRetryTime; }
}