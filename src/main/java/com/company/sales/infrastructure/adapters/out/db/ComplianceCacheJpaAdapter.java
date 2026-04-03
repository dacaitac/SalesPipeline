package com.company.sales.infrastructure.adapters.out.db;

import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.ComplianceCachePort;
import com.company.sales.infrastructure.adapters.out.db.entity.ComplianceCacheEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;

public class ComplianceCacheJpaAdapter implements ComplianceCachePort {
    private final EntityManagerFactory emf;

    public ComplianceCacheJpaAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<ValidationResult> getCachedResult(String nationalId) {
        EntityManager em = emf.createEntityManager();
        try {
            ComplianceCacheEntity entity = em.find(ComplianceCacheEntity.class, nationalId);
            if (entity != null) {
                return Optional.of(new ValidationResult(ValidationStatus.valueOf(entity.getStatus()), entity.getReason()));
            }
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public void saveResult(String nationalId, ValidationResult result) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(new ComplianceCacheEntity(nationalId, result.status().name(), result.reason()));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}