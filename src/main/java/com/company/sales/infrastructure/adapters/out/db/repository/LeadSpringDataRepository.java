package com.company.sales.infrastructure.adapters.out.db.repository;

import com.company.sales.infrastructure.adapters.out.db.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadSpringDataRepository extends JpaRepository<LeadEntity, String> {

    @Query("SELECT l FROM LeadEntity l WHERE l.validationStatus = 'PENDING' " +
            "AND l.retryCount > 0 AND l.nextRetryTime <= :now")
    List<LeadEntity> findPendingRetries(@Param("now") LocalDateTime now);
}