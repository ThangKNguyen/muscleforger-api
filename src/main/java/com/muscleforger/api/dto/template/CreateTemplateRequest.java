package com.muscleforger.api.dto.template;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateTemplateRequest(
        @NotBlank String name,
        @Min(1) @Max(7) int numberOfDays
) {}
