package com.muscleforger.api.dto.settings;

import jakarta.validation.constraints.Pattern;

public record UpdatePreferencesRequest(
        @Pattern(regexp = "^(dark|light)$", message = "Theme must be 'dark' or 'light'") String theme,
        @Pattern(regexp = "^(lbs|kg)$", message = "Weight unit must be 'lbs' or 'kg'") String weightUnit
) {}
