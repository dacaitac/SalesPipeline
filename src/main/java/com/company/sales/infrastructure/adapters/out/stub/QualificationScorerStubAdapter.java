package com.company.sales.infrastructure.adapters.out.stub;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.QualificationScorerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QualificationScorerStubAdapter implements QualificationScorerPort {
    private static final Logger log = LoggerFactory.getLogger(QualificationScorerStubAdapter.class);
    @Override
    public ValidationResult calculateScore(Lead lead) {
        int score = (int) (Math.random() * 100);
        log.info("Qualification score generated: {}", score);
        if (score > 60) return new ValidationResult(ValidationStatus.APPROVED, "Score: " + score);
        return new ValidationResult(ValidationStatus.REJECTED, "Score too low: " + score);
    }
}