package com.muscleforger.api.dto.template;

public record CustomExerciseResponse(
        String id,
        String name,
        String bodyPart,
        String target,
        String equipment,
        String gifUrl,
        String instructions,
        String description,
        boolean custom
) {}
