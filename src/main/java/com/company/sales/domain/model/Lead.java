package com.company.sales.domain.model;

import java.time.LocalDate;

public record Lead(
    String nationalId,
    LocalDate dateOfBirth,
    String firstName,
    String lastName,
    String email
) {}