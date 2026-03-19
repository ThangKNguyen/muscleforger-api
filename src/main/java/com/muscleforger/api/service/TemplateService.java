package com.muscleforger.api.service;

import com.muscleforger.api.dto.template.*;
import com.muscleforger.api.entity.*;
import com.muscleforger.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final WorkoutTemplateRepository templateRepository;
    private final TemplateDayRepository dayRepository;
    private final TemplateExerciseRepository exerciseRepository;
    private final ExerciseService exerciseService;

    // ── Templates ──────────────────────────────────────────────────────────────

    public List<TemplateSummaryResponse> getTemplates(User user) {
        return templateRepository.findByUserOrderByUpdatedAtDesc(user).stream()
                .map(t -> new TemplateSummaryResponse(
                        t.getId(), t.getName(),
                        dayRepository.countByTemplate(t),
                        t.getUpdatedAt()))
                .toList();
    }

    @Transactional
    public TemplateSummaryResponse createTemplate(User user, CreateTemplateRequest req) {
        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(user);
        template.setName(req.name());
        template = templateRepository.save(template);

        for (int i = 1; i <= req.numberOfDays(); i++) {
            TemplateDay day = new TemplateDay();
            day.setTemplate(template);
            day.setDayNumber((short) i);
            dayRepository.save(day);
        }

        return new TemplateSummaryResponse(
                template.getId(), template.getName(),
                req.numberOfDays(), template.getUpdatedAt());
    }

    public TemplateDetailResponse getTemplate(User user, Long templateId) {
        WorkoutTemplate template = findTemplateForUser(user, templateId);
        List<TemplateDayResponse> days = dayRepository
                .findByTemplateOrderByDayNumberAsc(template).stream()
                .map(day -> {
                    List<TemplateExerciseResponse> exercises = exerciseRepository
                            .findByDayOrderByPositionAsc(day).stream()
                            .map(ex -> new TemplateExerciseResponse(
                                    ex.getId(), ex.getExerciseId(), ex.getPosition(),
                                    ex.getSets(), ex.getReps(), ex.getRpe(),
                                    exerciseService.getExerciseById(ex.getExerciseId())))
                            .toList();
                    return new TemplateDayResponse(day.getId(), day.getDayNumber(), day.getLabel(), exercises);
                })
                .toList();
        return new TemplateDetailResponse(
                template.getId(), template.getName(),
                template.getCreatedAt(), template.getUpdatedAt(), days);
    }

    @Transactional
    public TemplateSummaryResponse updateTemplateName(User user, Long templateId, UpdateTemplateNameRequest req) {
        WorkoutTemplate template = findTemplateForUser(user, templateId);
        template.setName(req.name());
        touchTemplate(template);
        template = templateRepository.save(template);
        return new TemplateSummaryResponse(
                template.getId(), template.getName(),
                dayRepository.countByTemplate(template), template.getUpdatedAt());
    }

    @Transactional
    public void deleteTemplate(User user, Long templateId) {
        WorkoutTemplate template = findTemplateForUser(user, templateId);
        templateRepository.delete(template);
    }

    // ── Days ───────────────────────────────────────────────────────────────────

    @Transactional
    public TemplateDayResponse updateDayLabel(User user, Long templateId, Long dayId, UpdateDayLabelRequest req) {
        WorkoutTemplate template = findTemplateForUser(user, templateId);
        TemplateDay day = findDay(template, dayId);
        day.setLabel(req.label());
        dayRepository.save(day);
        touchTemplate(template);
        templateRepository.save(template);
        List<TemplateExerciseResponse> exercises = exerciseRepository
                .findByDayOrderByPositionAsc(day).stream()
                .map(ex -> new TemplateExerciseResponse(
                        ex.getId(), ex.getExerciseId(), ex.getPosition(),
                        ex.getSets(), ex.getReps(), ex.getRpe(),
                        exerciseService.getExerciseById(ex.getExerciseId())))
                .toList();
        return new TemplateDayResponse(day.getId(), day.getDayNumber(), day.getLabel(), exercises);
    }

    // ── Exercises ──────────────────────────────────────────────────────────────

    @Transactional
    public TemplateExerciseResponse addExerciseToDay(User user, Long templateId, Long dayId, AddExerciseToDayRequest req) {
        WorkoutTemplate template = findTemplateForUser(user, templateId);
        TemplateDay day = findDay(template, dayId);

        Integer maxPos = exerciseRepository.findMaxPositionByDay(day);
        short nextPos = (short) (maxPos == null ? 0 : maxPos + 1);

        TemplateExercise ex = new TemplateExercise();
        ex.setDay(day);
        ex.setExerciseId(req.exerciseId());
        ex.setPosition(nextPos);
        ex.setSets(req.sets());
        ex.setReps(req.reps());
        ex.setRpe(req.rpe());
        ex = exerciseRepository.save(ex);

        touchTemplate(template);
        templateRepository.save(template);

        return new TemplateExerciseResponse(
                ex.getId(), ex.getExerciseId(), ex.getPosition(),
                ex.getSets(), ex.getReps(), ex.getRpe(),
                exerciseService.getExerciseById(ex.getExerciseId()));
    }

    @Transactional
    public TemplateExerciseResponse updateExerciseConfig(User user, Long templateId, Long dayId, Long exerciseId, UpdateExerciseConfigRequest req) {
        WorkoutTemplate template = findTemplateForUser(user, templateId);
        TemplateDay day = findDay(template, dayId);
        TemplateExercise ex = exerciseRepository.findByIdAndDay(exerciseId, day)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found in day"));

        if (req.sets() != null) ex.setSets(req.sets());
        if (req.reps() != null) ex.setReps(req.reps());
        if (req.rpe() != null) ex.setRpe(req.rpe());
        ex = exerciseRepository.save(ex);

        touchTemplate(template);
        templateRepository.save(template);

        return new TemplateExerciseResponse(
                ex.getId(), ex.getExerciseId(), ex.getPosition(),
                ex.getSets(), ex.getReps(), ex.getRpe(),
                exerciseService.getExerciseById(ex.getExerciseId()));
    }

    @Transactional
    public void removeExerciseFromDay(User user, Long templateId, Long dayId, Long exerciseId) {
        WorkoutTemplate template = findTemplateForUser(user, templateId);
        TemplateDay day = findDay(template, dayId);
        TemplateExercise ex = exerciseRepository.findByIdAndDay(exerciseId, day)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found in day"));
        exerciseRepository.delete(ex);
        touchTemplate(template);
        templateRepository.save(template);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private WorkoutTemplate findTemplateForUser(User user, Long templateId) {
        return templateRepository.findByIdAndUser(templateId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
    }

    private TemplateDay findDay(WorkoutTemplate template, Long dayId) {
        return dayRepository.findByIdAndTemplate(dayId, template)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Day not found"));
    }

    private void touchTemplate(WorkoutTemplate template) {
        template.setUpdatedAt(LocalDateTime.now());
    }
}
