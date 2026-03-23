package com.muscleforger.api.dto.progress;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateWeightLogRequest(
        @NotNull @Positive BigDecimal weight,
        @NotNull LocalDate date
) {}
