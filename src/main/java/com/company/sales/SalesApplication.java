package com.company.sales;

import com.company.sales.infrastructure.adapters.in.LeadCliController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SalesApplication implements CommandLineRunner {

    private final LeadCliController cliController;

    public SalesApplication(LeadCliController cliController) {
        this.cliController = cliController;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SalesApplication.class);
        app.setLogStartupInfo(false);
        app.run(args);
    }

    @Override
    public void run(String... args) {
        cliController.start();
    }
}