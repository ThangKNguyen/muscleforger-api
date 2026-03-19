package com.muscleforger.api.dto.template;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomExerciseRequest(
        @NotBlank String name,
        @NotBlank String bodyPart,
        @NotBlank String target,
        @NotBlank String equipment,
        String instructions,
        String description
) {}
