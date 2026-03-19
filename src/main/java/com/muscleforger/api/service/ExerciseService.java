package com.muscleforger.api.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

    private final RestClient restClient;

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.exercisedb.host}")
    private String host;

    @Value("${rapidapi.exercisedb.base-url}")
    private String baseUrl;

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void warmCache() {
        CompletableFuture.runAsync(() -> {
            try {
                refreshCache();
            } catch (Exception e) {
                log.warn("Initial cache warm failed: {}", e.getMessage());
            }
        });
    }

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void refreshCache() {
        log.info("Refreshing exercise cache...");
        try {
            CompletableFuture<Void> bodyParts = CompletableFuture.runAsync(() ->
                    cache.put("bodyParts", fetch("/exercises/bodyPartList")));

            CompletableFuture<Void> allExercises = CompletableFuture.runAsync(() ->
                    cache.put("exercises:all", fetch("/exercises?limit=100")));

            CompletableFuture<Void> searchBase = CompletableFuture.runAsync(() ->
                    cache.put("exercises:search", fetch("/exercises?limit=900")));

            CompletableFuture.allOf(bodyParts, allExercises, searchBase).join();
            log.info("Exercise cache refreshed successfully.");
        } catch (Exception e) {
            log.warn("Cache refresh failed: {}", e.getMessage());
        }
    }

    public Object getBodyParts() {
        return cache.computeIfAbsent("bodyParts", k -> fetch("/exercises/bodyPartList"));
    }

    public Object getExercises(int limit) {
        return cache.computeIfAbsent("exercises:all", k -> fetch("/exercises?limit=" + limit));
    }

    public Object searchExercises(String query) {
        List<Map<String, Object>> all = (List<Map<String, Object>>)
                cache.computeIfAbsent("exercises:search", k -> fetch("/exercises?limit=900"));
        String q = query.toLowerCase();
        return all.stream()
                .filter(e -> matches(e, q))
                .collect(Collectors.toList());
    }

    public Object getByBodyPart(String bodyPart) {
        return cache.computeIfAbsent("exercises:bodyPart:" + bodyPart,
                k -> fetch("/exercises/bodyPart/" + bodyPart + "?limit=100"));
    }

    public Object getByTarget(String target) {
        return cache.computeIfAbsent("exercises:target:" + target,
                k -> fetch("/exercises/target/" + target + "?limit=100"));
    }

    public Object getByEquipment(String equipment) {
        return cache.computeIfAbsent("exercises:equipment:" + equipment,
                k -> fetch("/exercises/equipment/" + equipment + "?limit=100"));
    }

    public Object getExerciseById(String id) {
        return cache.computeIfAbsent("exercise:" + id,
                k -> fetch("/exercises/exercise/" + id));
    }

    private Object fetch(String path) {
        return restClient.get()
                .uri(baseUrl + path)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .retrieve()
                .body(Object.class);
    }

    private boolean matches(Map<String, Object> exercise, String query) {
        return fieldContains(exercise, "name", query)
                || fieldContains(exercise, "target", query)
                || fieldContains(exercise, "equipment", query)
                || fieldContains(exercise, "bodyPart", query);
    }

    private boolean fieldContains(Map<String, Object> exercise, String field, String query) {
        Object val = exercise.get(field);
        return val != null && val.toString().toLowerCase().contains(query);
    }
}
