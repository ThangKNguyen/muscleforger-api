package com.muscleforger.api.controller;

import com.muscleforger.api.service.ExerciseSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ExerciseSeederService seederService;

    @Value("${admin.seed-secret}")
    private String seedSecret;

    @PostMapping("/seed-exercises")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> seedExercises(
            @RequestHeader("X-Seed-Secret") String secret) {
        if (!seedSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        CompletableFuture.runAsync(seederService::seed);
        return Map.of("status", "Seed started. Check server logs for progress.");
    }

    @PostMapping("/seed-specific")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> seedSpecific(
            @RequestHeader("X-Seed-Secret") String secret) {
        if (!seedSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        CompletableFuture.runAsync(seederService::seedSpecific);
        return Map.of("status", "Specific seed started. Check server logs for progress.");
    }
}
