package com.muscleforger.api.dto.template;

import java.util.List;

public record TemplateDayResponse(
        Long id,
        Short dayNumber,
        String label,
        List<TemplateExerciseResponse> exercises
) {}
