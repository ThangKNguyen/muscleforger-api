package com.muscleforger.api.dto.template;

import jakarta.validation.constraints.NotNull;

public record ReorderExercisesRequest(
        @NotNull Long exerciseId1,
        @NotNull Long exerciseId2
) {}
