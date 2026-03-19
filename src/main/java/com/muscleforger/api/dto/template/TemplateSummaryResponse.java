package com.muscleforger.api.dto.template;

import java.time.LocalDateTime;

public record TemplateSummaryResponse(
        Long id,
        String name,
        long dayCount,
        LocalDateTime updatedAt
) {}
