package com.company.sales.infrastructure.adapters.out.json;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LeadJsonFileAdapterTest {

    private LeadJsonFileAdapter adapter;
    private final File testFile = new File("leads-repository.json");

    @BeforeEach
    void setUp() {
        // Limpia cualquier estado previo antes de instanciar el adaptador
        if (testFile.exists()) {
            testFile.delete();
        }
        adapter = new LeadJsonFileAdapter();
    }

    @AfterEach
    void tearDown() {
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void shouldSaveAndRetrieveLeadFromJsonFile() {
        Lead lead = new Lead("111", LocalDate.of(1990, 5, 5), "Bob", "Smith", "bob@test.com", ValidationStatus.PENDING, 0, null);
        
        adapter.save(lead);
        Optional<Lead> retrieved = adapter.findById("111");

        assertTrue(retrieved.isPresent());
        assertEquals("Bob", retrieved.get().firstName());
        assertEquals(ValidationStatus.PENDING, retrieved.get().validationStatus());
    }

    @Test
    void shouldUpdateExistingLeadInJsonFile() {
        Lead lead = new Lead("222", LocalDate.of(1990, 5, 5), "Alice", "Doe", "alice@test.com", ValidationStatus.PENDING, 0, null);
        adapter.save(lead);

        Lead updatedLead = lead.withValidationStatus(ValidationStatus.APPROVED);
        adapter.save(updatedLead);

        Optional<Lead> retrieved = adapter.findById("222");
        assertTrue(retrieved.isPresent());
        assertEquals(ValidationStatus.APPROVED, retrieved.get().validationStatus());
        assertEquals(1, adapter.findAll().size()); // Asegura que se reemplazó, no que se duplicó
    }

    @Test
    void shouldDeleteLeadFromJsonFile() {
        Lead lead = new Lead("333", LocalDate.of(1990, 5, 5), "Charlie", "Brown", "cb@test.com", ValidationStatus.PENDING, 0, null);
        adapter.save(lead);

        adapter.deleteById("333");

        assertFalse(adapter.findById("333").isPresent());
    }
}