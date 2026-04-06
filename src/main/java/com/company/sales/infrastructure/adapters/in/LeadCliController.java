package com.company.sales.infrastructure.adapters.in;

import com.company.sales.domain.exception.DomainValidationException;
import com.company.sales.domain.exception.ResourceNotFoundException;
import com.company.sales.domain.model.Lead;
import com.company.sales.domain.ports.in.LeadManagementUseCase;
import com.company.sales.domain.ports.in.LeadOrchestrationUseCase;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static com.company.sales.domain.model.ValidationStatus.PENDING;

@Component
public class LeadCliController {

    private final LeadOrchestrationUseCase leadOrchestrationUseCase;
    private final LeadManagementUseCase leadManagementUseCase;
    private final Scanner scanner;

    public LeadCliController(LeadOrchestrationUseCase leadOrchestrationUseCase, LeadManagementUseCase leadManagementUseCase) {
        this.leadOrchestrationUseCase = leadOrchestrationUseCase;
        this.leadManagementUseCase = leadManagementUseCase;
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
                case "1" -> handleValidateNewLead();
                case "2" -> handleViewAllLeads();
                case "3" -> handleUpdateLead();
                case "4" -> handleDeleteLead();
                case "0" -> {
                    running = false;
                    System.out.println("\n[INFO] Exiting Automated Lead Qualification Orchestrator. Goodbye.");
                }
                default -> System.out.println("\n[ERROR] Invalid option. Please select a valid number from the menu.");
            }
        }
    }

    // ... (printHeader y printMenu se mantienen igual) ...
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

        try {
            Lead lead = new Lead(id, LocalDate.of(1990, 1, 1), firstName, lastName, email, PENDING, 0, null);
            leadManagementUseCase.createLead(lead);

            System.out.println("\n[INFO] Lead saved as PENDING. Processing validation pipeline...");
            var result = leadOrchestrationUseCase.processLead(lead).join();

            System.out.println("\n--- VALIDATION RESULT ---");
            System.out.printf("%-15s: %s%n", "Final Status", result.status());
            System.out.printf("%-15s: %s%n", "Reason", result.reason());
            System.out.println("-------------------------");

        } catch (DomainValidationException e) {
            System.out.println("\n[VALIDATION ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[ERROR] Pipeline execution failed: " + e.getMessage());
        }
    }

    private void handleViewAllLeads() {
        System.out.println("\n--- LEAD DIRECTORY ---");
        List<Lead> leads = leadManagementUseCase.getAllLeads();
        printTable(leads);
    }

    private void printTable(List<Lead> leads) {
        String format = "| %-15s | %-15s | %-15s | %-25s | %-15s |%n";
        String separator = "+-----------------+-----------------+-----------------+---------------------------+-----------------+";

        System.out.println(separator);
        System.out.printf(format, "NATIONAL ID", "FIRST NAME", "LAST NAME", "EMAIL", "STATUS");
        System.out.println(separator);

        if (leads.isEmpty()) {
            System.out.printf("| %-93s |%n", "No records found.");
        } else {
            for (Lead lead : leads) {
                System.out.printf(format, lead.nationalId(), lead.firstName(), lead.lastName(), lead.email(), lead.validationStatus());
            }
        }
        System.out.println(separator);
    }

    private void handleUpdateLead() {
        System.out.println("\n--- UPDATE LEAD ---");
        System.out.print("Enter National ID to update: ");
        String id = scanner.nextLine();

        Optional<Lead> existingLead = leadManagementUseCase.getLead(id);
        if (existingLead.isEmpty()) {
            System.out.println("[ERROR] Lead not found.");
            return;
        }

        System.out.print("Enter New Email (leave blank to keep current): ");
        String newEmail = scanner.nextLine();

        Lead current = existingLead.get();
        String updatedEmail = newEmail.isEmpty() ? current.email() : newEmail;

        try {
            Lead updatedLead = new Lead(
                    current.nationalId(), current.dateOfBirth(), current.firstName(), current.lastName(),
                    updatedEmail, current.validationStatus(), current.retryCount(), current.nextRetryTime()
            );
            leadManagementUseCase.updateLead(updatedLead);
            System.out.println("[INFO] Lead updated successfully.");
        } catch (DomainValidationException e) {
            System.out.println("\n[VALIDATION ERROR] " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            System.out.println("\n[NOT FOUND ERROR] " + e.getMessage());
        }
    }

    private void handleDeleteLead() {
        System.out.println("\n--- DELETE LEAD ---");
        System.out.print("Enter National ID to delete: ");
        String id = scanner.nextLine();
        leadManagementUseCase.deleteLead(id);
        System.out.println("[INFO] Lead deleted successfully.");
    }
}