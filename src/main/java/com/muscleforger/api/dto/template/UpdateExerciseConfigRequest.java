package com.muscleforger.api.dto.template;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateExerciseConfigRequest(
        @Min(1) Short sets,
        @Min(1) Short reps,
        @DecimalMin("1.0") @DecimalMax("10.0") BigDecimal rpe
) {}
