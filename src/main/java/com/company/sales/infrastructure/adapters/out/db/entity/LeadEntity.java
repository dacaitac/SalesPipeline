package com.company.sales.infrastructure.adapters.out.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "leads")
public class LeadEntity {
    @Id
    private String nationalId;
    private LocalDate dateOfBirth;
    private String firstName;
    private String lastName;
    private String email;

    protected LeadEntity() {}

    public LeadEntity(String nationalId, LocalDate dateOfBirth, String firstName, String lastName, String email) {
        this.nationalId = nationalId;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getNationalId() { return nationalId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
}