package com.company.sales.infrastructure.adapters.out.json;

import com.company.sales.domain.model.Lead;
import com.company.sales.domain.model.ValidationStatus;
import com.company.sales.domain.ports.out.LeadRepositoryPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.repository.type", havingValue = "json")
public class LeadJsonFileAdapter implements LeadRepositoryPort {

    private final ObjectMapper mapper;
    private final File file;

    public LeadJsonFileAdapter() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.file = new File("leads-repository.json");
        initializeFile();
    }

    private void initializeFile() {
        if (!file.exists()) {
            saveAllToFile(new ArrayList<>());
        }
    }

    @Override
    public synchronized Lead save(Lead lead) {
        List<LeadJsonDto> dtos = readAllFromFile();
        dtos.removeIf(dto -> dto.nationalId.equals(lead.nationalId()));
        dtos.add(toDto(lead));
        saveAllToFile(dtos);
        return lead;
    }

    @Override
    public synchronized Optional<Lead> findById(String nationalId) {
        return readAllFromFile().stream()
                .filter(dto -> dto.nationalId.equals(nationalId))
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public synchronized List<Lead> findAll() {
        return readAllFromFile().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public synchronized void deleteById(String nationalId) {
        List<LeadJsonDto> dtos = readAllFromFile();
        dtos.removeIf(dto -> dto.nationalId.equals(nationalId));
        saveAllToFile(dtos);
    }

    @Override
    public List<Lead> findLeadsPendingForRetry(LocalDateTime currentTime) {
        return readAllFromFile().stream()
                .filter(dto -> "PENDING".equals(dto.validationStatus)
                        && dto.retryCount > 0
                        && dto.nextRetryTime != null
                        && dto.nextRetryTime.isBefore(currentTime))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private List<LeadJsonDto> readAllFromFile() {
        try {
            return mapper.readValue(file, new TypeReference<List<LeadJsonDto>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading JSON file", e);
        }
    }

    private void saveAllToFile(List<LeadJsonDto> dtos) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, dtos);
        } catch (IOException e) {
            throw new RuntimeException("Error writing JSON file", e);
        }
    }

    private LeadJsonDto toDto(Lead lead) {
        LeadJsonDto dto = new LeadJsonDto();
        dto.nationalId = lead.nationalId();
        dto.dateOfBirth = lead.dateOfBirth();
        dto.firstName = lead.firstName();
        dto.lastName = lead.lastName();
        dto.email = lead.email();
        dto.validationStatus = lead.validationStatus().name();
        dto.retryCount = lead.retryCount();
        dto.nextRetryTime = lead.nextRetryTime();
        return dto;
    }

    private Lead toDomain(LeadJsonDto dto) {
        return new Lead(
                dto.nationalId, dto.dateOfBirth, dto.firstName, dto.lastName, dto.email,
                ValidationStatus.valueOf(dto.validationStatus), dto.retryCount, dto.nextRetryTime
        );
    }
}