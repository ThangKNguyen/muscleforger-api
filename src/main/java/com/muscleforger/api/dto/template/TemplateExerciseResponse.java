package com.muscleforger.api.dto.template;

import java.math.BigDecimal;

public record TemplateExerciseResponse(
        Long id,
        String exerciseId,
        Short position,
        Short sets,
        Short reps,
        BigDecimal rpe,
        Object exerciseDetail
) {}
