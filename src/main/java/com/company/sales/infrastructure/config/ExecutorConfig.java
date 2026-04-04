package com.company.sales.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    // destroyMethod = "shutdown" le indica a Spring que cierre el pool 
    // ordenadamente cuando la aplicación se detenga.
    @Bean(destroyMethod = "shutdown")
    public ExecutorService leadValidationExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}