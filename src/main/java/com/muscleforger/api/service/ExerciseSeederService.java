package com.muscleforger.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscleforger.api.entity.Exercise;
import com.muscleforger.api.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseSeederService {

    private final ExerciseRepository exerciseRepository;
    private final FileStorageService fileStorageService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.exercisedb.host}")
    private String host;

    @Value("${rapidapi.exercisedb.base-url}")
    private String baseUrl;

    private static final int PAGE_SIZE = 10;

    // Each entry: (fetchType, value, limit)
    // fetchType: "bodyPart" or "target"
    private static final List<SeedGroup> SEED_GROUPS = List.of(
            new SeedGroup("bodyPart", "chest",       75),
            new SeedGroup("bodyPart", "back",        75),
            new SeedGroup("target",   "quads",       75),
            new SeedGroup("bodyPart", "shoulders",   75),
            new SeedGroup("target",   "triceps",     75),
            new SeedGroup("target",   "biceps",      75),
            new SeedGroup("target",   "hamstrings",  20),
            new SeedGroup("bodyPart", "lower arms",  20),
            new SeedGroup("target",   "abs",         20)
    );

    public SeedResult seed() {
        int totalSaved = 0;
        int totalGifs = 0;

        for (SeedGroup group : SEED_GROUPS) {
            log.info("Seeding {} '{}' (limit: {})", group.fetchType(), group.value(), group.limit());
            int offset = 0;
            int savedForGroup = 0;

            while (savedForGroup < group.limit()) {
                List<Map<String, Object>> page = fetchPage(group.fetchType(), group.value(), offset);
                if (page.isEmpty()) break;

                for (Map<String, Object> raw : page) {
                    if (savedForGroup >= group.limit()) break;

                    String id = (String) raw.get("id");
                    if (id == null || exerciseRepository.existsById(id)) {
                        savedForGroup++;
                        continue;
                    }

                    String gifUrl = fetchAndStoreGif(id);
                    if (gifUrl != null) totalGifs++;

                    exerciseRepository.save(toEntity(raw, gifUrl));
                    savedForGroup++;
                    totalSaved++;
                }

                offset += PAGE_SIZE;
                if (page.size() < PAGE_SIZE) break;
            }

            log.info("Saved {} exercises for {} '{}'", savedForGroup, group.fetchType(), group.value());
        }

        log.info("Seed complete. Exercises saved: {}, GIFs stored: {}", totalSaved, totalGifs);
        return new SeedResult(totalSaved, totalGifs);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchPage(String fetchType, String value, int offset) {
        try {
            String path = fetchType.equals("target")
                    ? "/exercises/target/" + encode(value)
                    : "/exercises/bodyPart/" + encode(value);
            Object response = restClient.get()
                    .uri(baseUrl + path + "?limit=" + PAGE_SIZE + "&offset=" + offset)
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", host)
                    .retrieve()
                    .body(Object.class);
            if (response instanceof List<?> list) return (List<Map<String, Object>>) list;
        } catch (Exception e) {
            log.warn("Failed to fetch page [{} '{}' offset {}]: {}", fetchType, value, offset, e.getMessage());
        }
        return List.of();
    }

    private String fetchAndStoreGif(String exerciseId) {
        try {
            byte[] gif = restClient.get()
                    .uri(baseUrl + "/image?resolution=360&exerciseId=" + exerciseId)
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", host)
                    .retrieve()
                    .body(byte[].class);
            if (gif == null || gif.length == 0) return null;
            return fileStorageService.uploadBytes("exercise-gifs", exerciseId + ".gif", gif, "image/gif");
        } catch (Exception e) {
            log.warn("Failed to fetch/store GIF for {}: {}", exerciseId, e.getMessage());
            return null;
        }
    }

    private Exercise toEntity(Map<String, Object> raw, String gifUrl) {
        Exercise e = new Exercise();
        e.setId((String) raw.get("id"));
        e.setName((String) raw.get("name"));
        e.setBodyPart((String) raw.get("bodyPart"));
        e.setTarget((String) raw.get("target"));
        e.setEquipment((String) raw.get("equipment"));
        e.setDescription((String) raw.get("description"));
        e.setDifficulty((String) raw.get("difficulty"));
        e.setCategory((String) raw.get("category"));
        e.setGifUrl(gifUrl);
        e.setSecondaryMuscles(toJson(raw.get("secondaryMuscles")));
        e.setInstructions(toJson(raw.get("instructions")));
        return e;
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String encode(String value) {
        return value.replace(" ", "%20");
    }

    public record SeedGroup(String fetchType, String value, int limit) {}
    public record SeedResult(int exercisesSaved, int gifsSaved) {}
}
