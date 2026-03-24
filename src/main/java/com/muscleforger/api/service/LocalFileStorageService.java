package com.muscleforger.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@Profile("local")
public class LocalFileStorageService implements FileStorageService {

    private final Path rootDir;
    private final String baseUrl;

    public LocalFileStorageService(
            @Value("${storage.local.dir:./uploads}") String dir,
            @Value("${storage.local.base-url:http://localhost:8080/uploads}") String baseUrl) {
        this.rootDir = Path.of(dir);
        this.baseUrl = baseUrl;
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String upload(String folder, String fileName, MultipartFile file) {
        try {
            Path target = rootDir.resolve(folder);
            Files.createDirectories(target);
            Path filePath = target.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return baseUrl + "/" + folder + "/" + fileName;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }

    @Override
    public String uploadBytes(String folder, String fileName, byte[] bytes, String contentType) {
        try {
            Path target = rootDir.resolve(folder);
            Files.createDirectories(target);
            Files.write(target.resolve(fileName), bytes);
            return baseUrl + "/" + folder + "/" + fileName;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store bytes", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) return;
        String relativePath = fileUrl.substring(baseUrl.length() + 1);
        try {
            Files.deleteIfExists(rootDir.resolve(relativePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file", e);
        }
    }
}
