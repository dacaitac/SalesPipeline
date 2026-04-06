package com.company.sales.application.service;

import com.company.sales.domain.exception.ResourceNotFoundException;
import com.company.sales.domain.model.Lead;
import com.company.sales.domain.ports.in.LeadManagementUseCase;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LeadManagementService implements LeadManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(LeadManagementService.class);
    private final LeadRepositoryPort leadRepositoryPort;

    public LeadManagementService(LeadRepositoryPort leadRepositoryPort) {
        this.leadRepositoryPort = leadRepositoryPort;
    }

    @Override
    public Lead createLead(Lead lead) {
        log.info("Creating new lead with National ID: {}", lead.nationalId());
        Lead savedLead = leadRepositoryPort.save(lead);
        log.info("Successfully created lead: {}", lead.nationalId());
        return savedLead;
    }

    @Override
    public Optional<Lead> getLead(String nationalId) {
        log.info("Fetching lead with National ID: {}", nationalId);
        return leadRepositoryPort.findById(nationalId);
    }

    @Override
    public List<Lead> getAllLeads() {
        log.info("Fetching all leads from the repository.");
        return leadRepositoryPort.findAll();
    }

    @Override
    public Lead updateLead(Lead lead) {
        log.info("Attempting to update lead with National ID: {}", lead.nationalId());
        if (leadRepositoryPort.findById(lead.nationalId()).isEmpty()) {
            log.error("Update failed. Lead with National ID {} does not exist.", lead.nationalId());
            throw new ResourceNotFoundException("Lead with National ID " + lead.nationalId() + " not found.");
        }
        Lead updatedLead = leadRepositoryPort.save(lead);
        log.info("Successfully updated lead: {}", lead.nationalId());
        return updatedLead;
    }

    @Override
    public void deleteLead(String nationalId) {
        log.info("Attempting to delete lead with National ID: {}", nationalId);
        leadRepositoryPort.deleteById(nationalId);
        log.info("Successfully deleted lead: {}", nationalId);
    }
}