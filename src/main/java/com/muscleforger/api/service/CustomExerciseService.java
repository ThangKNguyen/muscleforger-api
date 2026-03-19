package com.muscleforger.api.service;

import com.muscleforger.api.dto.template.CreateCustomExerciseRequest;
import com.muscleforger.api.dto.template.CustomExerciseResponse;
import com.muscleforger.api.entity.CustomExercise;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.repository.CustomExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomExerciseService {

    private final CustomExerciseRepository customExerciseRepository;

    public List<CustomExerciseResponse> getAll(User user) {
        return customExerciseRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::toResponse).toList();
    }

    public List<CustomExerciseResponse> search(User user, String q) {
        return customExerciseRepository.search(user, q)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public CustomExerciseResponse create(User user, CreateCustomExerciseRequest req) {
        CustomExercise e = new CustomExercise();
        e.setUser(user);
        e.setName(req.name());
        e.setBodyPart(req.bodyPart());
        e.setTarget(req.target());
        e.setEquipment(req.equipment());
        e.setInstructions(req.instructions());
        e.setDescription(req.description());
        return toResponse(customExerciseRepository.save(e));
    }

    @Transactional
    public CustomExerciseResponse update(User user, Long id, CreateCustomExerciseRequest req) {
        CustomExercise e = findOwned(user, id);
        e.setName(req.name());
        e.setBodyPart(req.bodyPart());
        e.setTarget(req.target());
        e.setEquipment(req.equipment());
        e.setInstructions(req.instructions());
        e.setDescription(req.description());
        return toResponse(customExerciseRepository.save(e));
    }

    @Transactional
    public void delete(User user, Long id) {
        CustomExercise e = findOwned(user, id);
        customExerciseRepository.delete(e);
    }

    // Called by ExerciseService to resolve a custom_ ID
    public CustomExerciseResponse getById(Long id) {
        return customExerciseRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Custom exercise not found"));
    }

    private CustomExercise findOwned(User user, Long id) {
        return customExerciseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Custom exercise not found"));
    }

    public CustomExerciseResponse toResponse(CustomExercise e) {
        return new CustomExerciseResponse(
                "custom_" + e.getId(),
                e.getName(),
                e.getBodyPart(),
                e.getTarget(),
                e.getEquipment(),
                null,
                e.getInstructions(),
                e.getDescription(),
                true
        );
    }
}
