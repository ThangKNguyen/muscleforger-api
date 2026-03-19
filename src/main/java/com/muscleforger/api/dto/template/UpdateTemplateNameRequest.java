package com.muscleforger.api.dto.template;

import jakarta.validation.constraints.NotBlank;

public record UpdateTemplateNameRequest(@NotBlank String name) {}
