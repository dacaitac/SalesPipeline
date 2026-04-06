package com.company.sales.infrastructure.adapters.out.db;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.infrastructure.adapters.in.LeadCliController;
import com.company.sales.infrastructure.adapters.out.db.repository.LeadSpringDataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "app.repository.type=h2")
@Import(LeadJpaAdapter.class)
@DisplayName("Infrastructure: Lead JPA Adapter Tests")
class LeadJpaAdapterTest {

    @MockitoBean
    private LeadCliController cliController;

    @Autowired
    private LeadJpaAdapter adapter;

    @Autowired
    private LeadSpringDataRepository repository;

    @Test
    @DisplayName("Should successfully persist and map a Lead domain object to entity and back")
    void shouldSaveAndRetrieveLead() {
        // Arrange
        Lead lead = new Lead("ID-999", LocalDate.of(1995, 10, 10), "Jane", "Doe",
                "jane@test.com", ValidationStatus.MANUAL_REVIEW, 2, null);

        // Act
        adapter.save(lead);
        Optional<Lead> retrieved = adapter.findById("ID-999");

        // Assert
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().firstName()).isEqualTo("Jane");
        assertThat(retrieved.get().validationStatus()).isEqualTo(ValidationStatus.MANUAL_REVIEW);
        assertThat(retrieved.get().retryCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should remove Lead entity from database when deleting by national ID")
    void shouldDeleteLead() {
        // Arrange
        Lead lead = new Lead("ID-888", LocalDate.of(1995, 10, 10), "Jane", "Doe",
                "jane@test.com", ValidationStatus.PENDING, 0, null);
        adapter.save(lead);

        // Act
        adapter.deleteById("ID-888");
        Optional<Lead> retrieved = adapter.findById("ID-888");

        // Assert
        assertThat(retrieved).isNotPresent();
    }
}