package com.company.sales.domain.ports.out;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;

public interface JudicialBackgroundPort {
    ValidationResult checkBackground(Lead lead);
}