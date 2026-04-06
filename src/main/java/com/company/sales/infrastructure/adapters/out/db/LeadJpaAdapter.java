package com.company.sales.infrastructure.adapters.out.db;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import com.company.sales.infrastructure.adapters.out.db.entity.LeadEntity;
import com.company.sales.infrastructure.adapters.out.db.repository.LeadSpringDataRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.repository.type", havingValue = "h2", matchIfMissing = true)
public class LeadJpaAdapter implements LeadRepositoryPort {

    private final LeadSpringDataRepository repository;

    public LeadJpaAdapter(LeadSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Lead save(Lead lead) {
        LeadEntity entity = new LeadEntity(
                lead.nationalId(), lead.dateOfBirth(), lead.firstName(), lead.lastName(),
                lead.email(), lead.validationStatus().name(), lead.retryCount(), lead.nextRetryTime()
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

    @Override
    public List<Lead> findLeadsPendingForRetry(LocalDateTime currentTime) {
        return repository.findPendingRetries(currentTime).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Lead toDomain(LeadEntity entity) {
        return new Lead(
                entity.getNationalId(), entity.getDateOfBirth(), entity.getFirstName(),
                entity.getLastName(), entity.getEmail(),
                ValidationStatus.valueOf(entity.getValidationStatus()),
                entity.getRetryCount(), entity.getNextRetryTime()
        );
    }

}