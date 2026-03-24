package com.muscleforger.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscleforger.api.entity.Exercise;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final CustomExerciseService customExerciseService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.exercisedb.host}")
    private String host;

    @Value("${rapidapi.exercisedb.base-url}")
    private String baseUrl;

    // ── Public API ────────────────────────────────────────────────────────────────

    public List<String> getBodyParts() {
        return exerciseRepository.findDistinctBodyParts();
    }

    public List<Map<String, Object>> getExercises() {
        return exerciseRepository.findAll().stream().map(this::toMap).toList();
    }

    public Object searchExercises(String query) {
        return searchExercises(query, null);
    }

    public Object searchExercises(String query, User user) {
        List<Object> results = new java.util.ArrayList<>(
                exerciseRepository.search(query).stream()
                        .map(e -> (Object) toMap(e))
                        .toList());

        if (user != null) {
            List<Object> custom = customExerciseService.search(user, query)
                    .stream().map(r -> (Object) r).toList();
            results.addAll(0, custom);
        }
        return results;
    }

    public List<Map<String, Object>> getByBodyPart(String bodyPart) {
        return exerciseRepository.findByBodyPartIgnoreCase(bodyPart)
                .stream().map(this::toMap).toList();
    }

    public List<Map<String, Object>> getByTarget(String target) {
        return exerciseRepository.findByTargetIgnoreCase(target)
                .stream().map(this::toMap).toList();
    }

    public List<Map<String, Object>> getByEquipment(String equipment) {
        return exerciseRepository.findByEquipmentIgnoreCase(equipment)
                .stream().map(this::toMap).toList();
    }

    public Object getExerciseById(String id) {
        if (id.startsWith("custom_")) {
            try {
                long numericId = Long.parseLong(id.substring(7));
                return customExerciseService.getById(numericId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return exerciseRepository.findById(id)
                .map(this::toMap)
                .orElse(null);
    }

    public byte[] getExerciseGif(String exerciseId) {
        return restClient.get()
                .uri(baseUrl + "/image?resolution=360&exerciseId=" + exerciseId)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .retrieve()
                .body(byte[].class);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private Map<String, Object> toMap(Exercise e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", e.getId());
        map.put("name", e.getName());
        map.put("bodyPart", e.getBodyPart());
        map.put("target", e.getTarget());
        map.put("equipment", e.getEquipment());
        map.put("gifUrl", e.getGifUrl());
        map.put("secondaryMuscles", parseJson(e.getSecondaryMuscles()));
        map.put("instructions", parseJson(e.getInstructions()));
        map.put("description", e.getDescription());
        map.put("difficulty", e.getDifficulty());
        map.put("category", e.getCategory());
        return map;
    }

    private Object parseJson(String json) {
        if (json == null) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }
}
