package com.muscleforger.api.dto.auth;

import java.time.LocalDateTime;

public record UserResponse(Long id, String email, String username, String avatarUrl, LocalDateTime createdAt) {}
