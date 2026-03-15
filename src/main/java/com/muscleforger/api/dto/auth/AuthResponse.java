package com.muscleforger.api.dto.auth;

public record AuthResponse(String token, String refreshToken, UserResponse user) {}
