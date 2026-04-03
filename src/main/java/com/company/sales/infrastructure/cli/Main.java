package com.company.sales.infrastructure.cli;

import com.company.sales.application.service.LeadOrchestrator;
import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationResult;
import com.company.sales.domain.ports.out.*;
import com.company.sales.infrastructure.adapters.out.db.ComplianceCacheJpaAdapter;
import com.company.sales.infrastructure.adapters.out.stub.ComplianceBureauStubAdapter;
import com.company.sales.infrastructure.adapters.out.stub.JudicialBackgroundStubAdapter;
import com.company.sales.infrastructure.adapters.out.stub.NationalRegistryStubAdapter;
import com.company.sales.infrastructure.adapters.out.stub.QualificationScorerStubAdapter;
import com.company.sales.infrastructure.decorators.ComplianceCachingDecorator;
import com.company.sales.infrastructure.decorators.ComplianceResilienceDecorator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        // 1. Thread Pool Configuration
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 2. Persistence Configuration (JPA H2)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("lead-pu");
        ComplianceCachePort cachePort = new ComplianceCacheJpaAdapter(emf);

        // 3. Dependency Injection (Ports and Adapters)
        NationalRegistryPort registryPort = new NationalRegistryStubAdapter();
        JudicialBackgroundPort judicialPort = new JudicialBackgroundStubAdapter();
        QualificationScorerPort scorerPort = new QualificationScorerStubAdapter();
        
        // Building the Compliance Port with Decorators (Resilience wraps Cache wraps Stub)
        ComplianceBureauPort baseCompliancePort = new ComplianceBureauStubAdapter();
        ComplianceBureauPort cachingCompliancePort = new ComplianceCachingDecorator(baseCompliancePort, cachePort);
        ComplianceBureauPort resilientCompliancePort = new ComplianceResilienceDecorator(cachingCompliancePort);

        // 4. Orchestrator Initialization
        LeadOrchestrator orchestrator = new LeadOrchestrator(
                registryPort, 
                judicialPort, 
                resilientCompliancePort, 
                scorerPort, 
                executor
        );

        // 5. Execution
        Lead lead = new Lead("123456789", LocalDate.of(1990, 5, 15), "Juan", "Perez", "juan@example.com");
        
        System.out.println("--- Starting Lead Validation Process ---");
        
        ValidationResult finalResult = orchestrator.processLead(lead).join();
        
        System.out.println("--- Process Completed ---");
        System.out.println("Final Status: " + finalResult.status());
        System.out.println("Reason: " + finalResult.reason());

        // Cleanup
        executor.shutdown();
        emf.close();
    }
}