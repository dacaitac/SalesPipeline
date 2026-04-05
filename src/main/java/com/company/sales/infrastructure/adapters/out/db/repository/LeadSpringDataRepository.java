package com.company.sales.infrastructure.adapters.out.db.repository;

import com.company.sales.infrastructure.adapters.out.db.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadSpringDataRepository extends JpaRepository<LeadEntity, String> {
}