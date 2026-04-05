package com.company.sales.infrastructure.adapters.out.db;

import com.company.sales.domain.model.Lead;
import com.company.sales.infrastructure.adapters.in.LeadCliController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LeadJpaAdapterTest {

    @Autowired
    private LeadJpaAdapter adapter;

    @MockitoBean
    private LeadCliController cliController;

    @Test
    void shouldSaveAndRetrieveLead() {
        Lead lead = new Lead("ID-999", LocalDate.of(1995, 10, 10), "Jane", "Doe", "jane@test.com");

        adapter.save(lead);

        Optional<Lead> retrieved = adapter.findById("ID-999");
        assertTrue(retrieved.isPresent());
        assertEquals("Jane", retrieved.get().firstName());
    }

    @Test
    void shouldDeleteLead() {
        Lead lead = new Lead("ID-888", LocalDate.of(1995, 10, 10), "Jane", "Doe", "jane@test.com");
        adapter.save(lead);

        adapter.deleteById("ID-888");

        Optional<Lead> retrieved = adapter.findById("ID-888");
        assertFalse(retrieved.isPresent());
    }
}