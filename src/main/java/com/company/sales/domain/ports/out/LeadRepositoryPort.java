package com.company.sales.domain.ports.out;

import com.company.sales.domain.model.Lead;
import java.util.List;
import java.util.Optional;

public interface LeadRepositoryPort {
    Lead save(Lead lead);
    Optional<Lead> findById(String nationalId);
    List<Lead> findAll();
    void deleteById(String nationalId);
}