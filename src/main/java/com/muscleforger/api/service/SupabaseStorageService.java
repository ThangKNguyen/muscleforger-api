package com.muscleforger.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@Service
@Profile("!local")
public class SupabaseStorageService implements FileStorageService {

    private final String supabaseUrl;
    private final String supabaseKey;
    private final String bucket;
    private final RestTemplate restTemplate = new RestTemplate();

    public SupabaseStorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String supabaseKey,
            @Value("${supabase.storage.bucket:uploads}") String bucket) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        this.bucket = bucket;
    }

    @Override
    public String upload(String folder, String fileName, MultipartFile file) {
        String path = folder + "/" + fileName;
        String url = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream"));
        // upsert so re-uploading avatar replaces the old one
        headers.set("x-upsert", "true");

        try {
            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file bytes", e);
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    @Override
    public String uploadBytes(String folder, String fileName, byte[] bytes, String contentType) {
        String path = folder + "/" + fileName;
        String url = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set("x-upsert", "true");

        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(bytes, headers), String.class);
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null) return;

        // Extract path after /storage/v1/object/public/{bucket}/
        String prefix = supabaseUrl + "/storage/v1/object/public/" + bucket + "/";
        if (!fileUrl.startsWith(prefix)) return;

        String path = fileUrl.substring(prefix.length());
        String url = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);

        restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
    }
}
