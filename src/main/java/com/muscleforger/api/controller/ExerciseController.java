package com.muscleforger.api.controller;

import com.muscleforger.api.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping("/body-parts")
    public Object getBodyParts() {
        return exerciseService.getBodyParts();
    }

    @GetMapping
    public Object getExercises(@RequestParam(defaultValue = "100") int limit) {
        return exerciseService.getExercises(limit);
    }

    @GetMapping("/search")
    public Object search(@RequestParam String q) {
        return exerciseService.searchExercises(q);
    }

    @GetMapping("/body-part/{bodyPart}")
    public Object getByBodyPart(@PathVariable String bodyPart) {
        return exerciseService.getByBodyPart(bodyPart);
    }

    @GetMapping("/target/{target}")
    public Object getByTarget(@PathVariable String target) {
        return exerciseService.getByTarget(target);
    }

    @GetMapping("/equipment/{equipment}")
    public Object getByEquipment(@PathVariable String equipment) {
        return exerciseService.getByEquipment(equipment);
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable String id) {
        return exerciseService.getExerciseById(id);
    }
}
