package com.muscleforger.api.controller;

import com.muscleforger.api.dto.user.ExerciseIdRequest;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.repository.UserRepository;
import com.muscleforger.api.service.UserDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserDataController {

    private final UserDataService userDataService;
    private final UserRepository userRepository;

    // ── Favorites ──────────────────────────────────────────────────────────────

    @GetMapping("/favorites")
    public List<Object> getFavorites(Authentication auth) {
        return userDataService.getFavorites(getUser(auth));
    }

    @PostMapping("/favorites")
    @ResponseStatus(HttpStatus.CREATED)
    public void addFavorite(Authentication auth, @Valid @RequestBody ExerciseIdRequest body) {
        userDataService.addFavorite(getUser(auth), body.exerciseId());
    }

    @DeleteMapping("/favorites/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(Authentication auth, @PathVariable String exerciseId) {
        userDataService.removeFavorite(getUser(auth), exerciseId);
    }

    // ── Workout ────────────────────────────────────────────────────────────────

    @GetMapping("/workout")
    public List<Object> getWorkout(Authentication auth) {
        return userDataService.getWorkout(getUser(auth));
    }

    @PostMapping("/workout")
    @ResponseStatus(HttpStatus.CREATED)
    public void addToWorkout(Authentication auth, @Valid @RequestBody ExerciseIdRequest body) {
        userDataService.addToWorkout(getUser(auth), body.exerciseId());
    }

    @DeleteMapping("/workout/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromWorkout(Authentication auth, @PathVariable String exerciseId) {
        userDataService.removeFromWorkout(getUser(auth), exerciseId);
    }

    @DeleteMapping("/workout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearWorkout(Authentication auth) {
        userDataService.clearWorkout(getUser(auth));
    }

    // ── History ────────────────────────────────────────────────────────────────

    @GetMapping("/history")
    public List<Object> getHistory(Authentication auth) {
        return userDataService.getHistory(getUser(auth));
    }

    @PostMapping("/history")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logView(Authentication auth, @Valid @RequestBody ExerciseIdRequest body) {
        userDataService.logView(getUser(auth), body.exerciseId());
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
