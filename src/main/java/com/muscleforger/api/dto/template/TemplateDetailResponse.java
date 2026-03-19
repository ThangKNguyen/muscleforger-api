package com.muscleforger.api.dto.template;

import java.time.LocalDateTime;
import java.util.List;

public record TemplateDetailResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TemplateDayResponse> days
) {}
