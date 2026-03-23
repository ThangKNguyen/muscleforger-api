package com.muscleforger.api.dto.settings;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 3, max = 100) String username,
        @Email String email
) {}
