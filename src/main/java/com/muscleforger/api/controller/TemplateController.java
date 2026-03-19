package com.muscleforger.api.controller;

import com.muscleforger.api.dto.template.*;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.repository.UserRepository;
import com.muscleforger.api.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/user/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final UserRepository userRepository;

    // ── Templates ──────────────────────────────────────────────────────────────

    @GetMapping
    public List<TemplateSummaryResponse> getTemplates(Authentication auth) {
        return templateService.getTemplates(getUser(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateSummaryResponse createTemplate(Authentication auth,
                                                   @Valid @RequestBody CreateTemplateRequest body) {
        return templateService.createTemplate(getUser(auth), body);
    }

    @GetMapping("/{templateId}")
    public TemplateDetailResponse getTemplate(Authentication auth, @PathVariable Long templateId) {
        return templateService.getTemplate(getUser(auth), templateId);
    }

    @PatchMapping("/{templateId}")
    public TemplateSummaryResponse updateTemplateName(Authentication auth, @PathVariable Long templateId,
                                                       @Valid @RequestBody UpdateTemplateNameRequest body) {
        return templateService.updateTemplateName(getUser(auth), templateId, body);
    }

    @DeleteMapping("/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(Authentication auth, @PathVariable Long templateId) {
        templateService.deleteTemplate(getUser(auth), templateId);
    }

    // ── Days ───────────────────────────────────────────────────────────────────

    @PatchMapping("/{templateId}/days/{dayId}")
    public TemplateDayResponse updateDayLabel(Authentication auth,
                                               @PathVariable Long templateId,
                                               @PathVariable Long dayId,
                                               @RequestBody UpdateDayLabelRequest body) {
        return templateService.updateDayLabel(getUser(auth), templateId, dayId, body);
    }

    // ── Exercises ──────────────────────────────────────────────────────────────

    @PostMapping("/{templateId}/days/{dayId}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateExerciseResponse addExercise(Authentication auth,
                                                 @PathVariable Long templateId,
                                                 @PathVariable Long dayId,
                                                 @Valid @RequestBody AddExerciseToDayRequest body) {
        return templateService.addExerciseToDay(getUser(auth), templateId, dayId, body);
    }

    @PatchMapping("/{templateId}/days/{dayId}/exercises/{exerciseId}")
    public TemplateExerciseResponse updateExercise(Authentication auth,
                                                    @PathVariable Long templateId,
                                                    @PathVariable Long dayId,
                                                    @PathVariable Long exerciseId,
                                                    @Valid @RequestBody UpdateExerciseConfigRequest body) {
        return templateService.updateExerciseConfig(getUser(auth), templateId, dayId, exerciseId, body);
    }

    @DeleteMapping("/{templateId}/days/{dayId}/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeExercise(Authentication auth,
                                @PathVariable Long templateId,
                                @PathVariable Long dayId,
                                @PathVariable Long exerciseId) {
        templateService.removeExerciseFromDay(getUser(auth), templateId, dayId, exerciseId);
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
