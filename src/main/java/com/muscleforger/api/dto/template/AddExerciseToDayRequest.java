package com.muscleforger.api.dto.template;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddExerciseToDayRequest(
        @NotBlank String exerciseId,
        @NotNull @Min(1) Short sets,
        @NotNull @Min(1) Short reps,
        @DecimalMin("1.0") @DecimalMax("10.0") BigDecimal rpe,
        String notes
) {}
