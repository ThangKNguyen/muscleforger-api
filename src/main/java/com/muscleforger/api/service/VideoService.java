package com.muscleforger.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final RestClient restClient;

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.youtube.host}")
    private String host;

    @Value("${rapidapi.youtube.base-url}")
    private String baseUrl;

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    public Object searchVideos(String exerciseName) {
        return cache.computeIfAbsent("videos:" + exerciseName.toLowerCase(), k -> {
            String query = UriUtils.encode(exerciseName + " exercise", StandardCharsets.UTF_8);
            return restClient.get()
                    .uri(baseUrl + "/search?query=" + query)
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", host)
                    .retrieve()
                    .body(Object.class);
        });
    }
}
