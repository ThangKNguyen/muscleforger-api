package com.muscleforger.api.controller;

import com.muscleforger.api.dto.template.CreateCustomExerciseRequest;
import com.muscleforger.api.dto.template.CustomExerciseResponse;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.repository.UserRepository;
import com.muscleforger.api.service.CustomExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/user/custom-exercises")
@RequiredArgsConstructor
public class CustomExerciseController {

    private final CustomExerciseService customExerciseService;
    private final UserRepository userRepository;

    @GetMapping
    public List<CustomExerciseResponse> getAll(Authentication auth,
                                               @RequestParam(required = false) String q) {
        User user = getUser(auth);
        return (q != null && !q.isBlank())
                ? customExerciseService.search(user, q)
                : customExerciseService.getAll(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomExerciseResponse create(Authentication auth,
                                         @Valid @RequestBody CreateCustomExerciseRequest body) {
        return customExerciseService.create(getUser(auth), body);
    }

    @PutMapping("/{id}")
    public CustomExerciseResponse update(Authentication auth, @PathVariable Long id,
                                          @Valid @RequestBody CreateCustomExerciseRequest body) {
        return customExerciseService.update(getUser(auth), id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        customExerciseService.delete(getUser(auth), id);
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
