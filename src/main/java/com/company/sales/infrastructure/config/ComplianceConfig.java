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
        // 1. Instancia base
        ComplianceBureauPort base = new ComplianceBureauStubAdapter();
        // 2. Envuelve con caché
        ComplianceBureauPort cached = new ComplianceCachingDecorator(base, cachePort);
        // 3. Envuelve con resiliencia (este es el bean final que Spring inyectará en el Orquestador)
        return new ComplianceResilienceDecorator(cached);
    }
}