package com.company.sales.application.service;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.ports.in.LeadManagementUseCase;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LeadManagementService implements LeadManagementUseCase {

    private final LeadRepositoryPort leadRepositoryPort;

    public LeadManagementService(LeadRepositoryPort leadRepositoryPort) {
        this.leadRepositoryPort = leadRepositoryPort;
    }

    @Override
    public Lead createLead(Lead lead) {
        return leadRepositoryPort.save(lead);
    }

    @Override
    public Optional<Lead> getLead(String nationalId) {
        return leadRepositoryPort.findById(nationalId);
    }

    @Override
    public List<Lead> getAllLeads() {
        return leadRepositoryPort.findAll();
    }

    @Override
    public Lead updateLead(Lead lead) {
        if (leadRepositoryPort.findById(lead.nationalId()).isEmpty()) {
            throw new IllegalArgumentException("Lead does not exist");
        }
        return leadRepositoryPort.save(lead);
    }

    @Override
    public void deleteLead(String nationalId) {
        leadRepositoryPort.deleteById(nationalId);
    }
}