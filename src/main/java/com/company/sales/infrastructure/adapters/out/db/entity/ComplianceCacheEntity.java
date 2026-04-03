package com.company.sales.infrastructure.adapters.out.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "compliance_cache")
public class ComplianceCacheEntity {
    @Id
    private String nationalId;
    private String status;
    private String reason;

    // Constructors, Getters, Setters
    protected ComplianceCacheEntity() {}
    public ComplianceCacheEntity(String nationalId, String status, String reason) {
        this.nationalId = nationalId; this.status = status; this.reason = reason;
    }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
}