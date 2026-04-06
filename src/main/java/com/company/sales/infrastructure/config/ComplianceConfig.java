package com.company.sales.infrastructure.config;

import com.company.sales.domain.ports.out.ComplianceBureauPort;
import com.company.sales.domain.ports.out.ComplianceCachePort;
import com.company.sales.infrastructure.adapters.out.stub.ComplianceBureauStubAdapter;
import com.company.sales.infrastructure.decorators.ComplianceCachingDecorator;
import com.company.sales.infrastructure.decorators.ComplianceResilienceDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComplianceConfig {

    @Bean
    public ComplianceBureauPort complianceBureauPort(ComplianceCachePort cachePort) {
        ComplianceBureauPort base = new ComplianceBureauStubAdapter();
        ComplianceBureauPort cached = new ComplianceCachingDecorator(base, cachePort);

        return new ComplianceResilienceDecorator(cached);
    }
}