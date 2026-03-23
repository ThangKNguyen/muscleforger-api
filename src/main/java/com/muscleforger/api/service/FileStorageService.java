package com.muscleforger.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Upload a file and return its public URL.
     *
     * @param folder subfolder path (e.g. "avatars", "progress-photos")
     * @param fileName the file name to store as
     * @param file the uploaded file
     * @return the public URL of the stored file
     */
    String upload(String folder, String fileName, MultipartFile file);

    /**
     * Delete a file by its URL.
     */
    void delete(String fileUrl);
}
