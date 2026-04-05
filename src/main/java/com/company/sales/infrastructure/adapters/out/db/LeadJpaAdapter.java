package com.company.sales.infrastructure.adapters.out.db;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import com.company.sales.infrastructure.adapters.out.db.entity.LeadEntity;
import com.company.sales.infrastructure.adapters.out.db.repository.LeadSpringDataRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LeadJpaAdapter implements LeadRepositoryPort {

    private final LeadSpringDataRepository repository;

    public LeadJpaAdapter(LeadSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Lead save(Lead lead) {
        LeadEntity entity = new LeadEntity(
                lead.nationalId(), lead.dateOfBirth(), lead.firstName(), lead.lastName(), lead.email()
        );
        repository.save(entity);
        return lead;
    }

    @Override
    public Optional<Lead> findById(String nationalId) {
        return repository.findById(nationalId).map(this::toDomain);
    }

    @Override
    public List<Lead> findAll() {
        return repository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(String nationalId) {
        repository.deleteById(nationalId);
    }

    private Lead toDomain(LeadEntity entity) {
        return new Lead(
                entity.getNationalId(), entity.getDateOfBirth(), entity.getFirstName(), entity.getLastName(), entity.getEmail()
        );
    }
}