package com.muscleforger.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Upload a file and return its public URL.
     */
    String upload(String folder, String fileName, MultipartFile file);

    /**
     * Upload raw bytes and return the public URL. Used by the seeder.
     */
    String uploadBytes(String folder, String fileName, byte[] bytes, String contentType);

    /**
     * Delete a file by its URL.
     */
    void delete(String fileUrl);
}
