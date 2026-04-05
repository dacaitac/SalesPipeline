package com.company.sales.domain.ports.in;

import com.company.sales.domain.model.Lead;
import java.util.List;
import java.util.Optional;

public interface LeadManagementUseCase {
    Lead createLead(Lead lead);
    Optional<Lead> getLead(String nationalId);
    List<Lead> getAllLeads();
    Lead updateLead(Lead lead);
    void deleteLead(String nationalId);
}