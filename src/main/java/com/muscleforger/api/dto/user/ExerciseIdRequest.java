package com.muscleforger.api.dto.user;

import jakarta.validation.constraints.NotBlank;

public record ExerciseIdRequest(@NotBlank String exerciseId) {}
