package com.muscleforger.api.controller;

import com.muscleforger.api.dto.progress.CreateWeightLogRequest;
import com.muscleforger.api.dto.progress.WeightLogResponse;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.repository.UserRepository;
import com.muscleforger.api.service.WeightLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/weight-logs")
@RequiredArgsConstructor
public class WeightLogController {

    private final WeightLogService weightLogService;
    private final UserRepository userRepository;

    @GetMapping
    public List<WeightLogResponse> getLogs(
            Authentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return weightLogService.getLogs(getUser(auth), from, to);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WeightLogResponse create(Authentication auth,
                                    @Valid @RequestBody CreateWeightLogRequest request) {
        return weightLogService.create(getUser(auth), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        weightLogService.delete(getUser(auth), id);
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
