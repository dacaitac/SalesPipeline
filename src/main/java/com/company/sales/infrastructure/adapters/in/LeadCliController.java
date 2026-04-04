package com.company.sales.infrastructure.adapters.in;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.ports.in.LeadOrchestrationUseCase;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

@Component
public class LeadCliController {

    private final LeadOrchestrationUseCase leadOrchestrationUseCase;
    private final Scanner scanner;

    public LeadCliController(LeadOrchestrationUseCase leadOrchestrationUseCase) {
        this.leadOrchestrationUseCase = leadOrchestrationUseCase;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printHeader();
            printMenu();
            
            System.out.print("Select an option [0-4]: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    handleValidateNewLead();
                    break;
                case "2":
                    handleViewAllLeads();
                    break;
                case "3":
                    handleUpdateLead();
                    break;
                case "4":
                    handleDeleteLead();
                    break;
                case "0":
                    running = false;
                    System.out.println("\n[INFO] Exiting Automated Lead Qualification Orchestrator. Goodbye.");
                    break;
                default:
                    System.out.println("\n[ERROR] Invalid option. Please select a valid number from the menu.");
            }
        }
    }

    private void printHeader() {
        System.out.println("\n===============================================================================");
        System.out.println("                 AUTOMATED LEAD QUALIFICATION ORCHESTRATOR                     ");
        System.out.println("===============================================================================");
    }

    private void printMenu() {
        System.out.println("  [1] Validate New Lead");
        System.out.println("  [2] View All Leads");
        System.out.println("  [3] Update Lead");
        System.out.println("  [4] Delete Lead");
        System.out.println("  [0] Exit System");
        System.out.println("-------------------------------------------------------------------------------");
    }

    private void handleValidateNewLead() {
        System.out.println("\n--- VALIDATE NEW LEAD ---");
        System.out.print("Enter National ID: ");
        String id = scanner.nextLine();

        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine();

        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        Lead lead = new Lead(id, LocalDate.of(1990, 1, 1), firstName, lastName, email);

        System.out.println("\n[INFO] Processing validation pipeline...");
        
        try {
            var result = leadOrchestrationUseCase.processLead(lead).join();
            System.out.println("\n--- VALIDATION RESULT ---");
            System.out.printf("%-15s: %s%n", "Status", result.status());
            System.out.printf("%-15s: %s%n", "Reason", result.reason());
            System.out.println("-------------------------");
        } catch (Exception e) {
            System.out.println("\n[ERROR] Pipeline execution failed: " + e.getMessage());
        }
    }

    private void handleViewAllLeads() {
        System.out.println("\n--- LEAD DIRECTORY ---");
        
        // Simulación de datos recuperados de la base de datos
        List<Lead> dummyLeads = List.of(
                new Lead("123456789", LocalDate.of(1990, 5, 15), "John", "Doe", "john.doe@example.com"),
                new Lead("987654321", LocalDate.of(1985, 8, 22), "Jane", "Smith", "jane.smith@example.com")
        );

        printTable(dummyLeads);
    }

    private void printTable(List<Lead> leads) {
        String format = "| %-15s | %-15s | %-15s | %-25s |%n";
        String separator = "+-----------------+-----------------+-----------------+---------------------------+";

        System.out.println(separator);
        System.out.printf(format, "NATIONAL ID", "FIRST NAME", "LAST NAME", "EMAIL");
        System.out.println(separator);

        if (leads.isEmpty()) {
            System.out.printf("| %-71s |%n", "No records found.");
        } else {
            for (Lead lead : leads) {
                System.out.printf(format, 
                        lead.nationalId(), 
                        lead.firstName(), 
                        lead.lastName(), 
                        lead.email());
            }
        }
        System.out.println(separator);
    }

    private void handleUpdateLead() {
        System.out.println("\n--- UPDATE LEAD ---");
        System.out.print("Enter National ID to update: ");
        scanner.nextLine();
        System.out.println("[INFO] Update module is currently under construction.");
    }

    private void handleDeleteLead() {
        System.out.println("\n--- DELETE LEAD ---");
        System.out.print("Enter National ID to delete: ");
        scanner.nextLine();
        System.out.println("[INFO] Delete module is currently under construction.");
    }
}