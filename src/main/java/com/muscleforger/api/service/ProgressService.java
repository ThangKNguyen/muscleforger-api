package com.muscleforger.api.service;

import com.muscleforger.api.dto.progress.ProgressEntryResponse;
import com.muscleforger.api.dto.progress.ProgressPhotoResponse;
import com.muscleforger.api.entity.ProgressEntry;
import com.muscleforger.api.entity.ProgressPhoto;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.repository.ProgressEntryRepository;
import com.muscleforger.api.repository.ProgressPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressEntryRepository entryRepository;
    private final ProgressPhotoRepository photoRepository;
    private final FileStorageService fileStorageService;

    public List<ProgressEntryResponse> getEntries(User user, Integer year) {
        List<ProgressEntry> entries;
        if (year != null) {
            entries = entryRepository.findByUserAndYear(user, year);
        } else {
            entries = entryRepository.findByUserOrderByDateDesc(user);
        }
        return entries.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProgressEntryResponse createEntry(User user, LocalDate date, String caption, List<MultipartFile> files) {
        ProgressEntry entry = new ProgressEntry();
        entry.setUser(user);
        entry.setDate(date);
        entry.setCaption(caption != null ? caption : "");
        entryRepository.save(entry);

        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                ProgressPhoto photo = uploadPhoto(entry, files.get(i), i);
                entry.getPhotos().add(photo);
            }
        }

        return toResponse(entry);
    }

    @Transactional
    public ProgressEntryResponse addPhotos(User user, Long entryId, List<MultipartFile> files) {
        ProgressEntry entry = getOwnedEntry(user, entryId);
        int nextOrder = entry.getPhotos().size();
        for (int i = 0; i < files.size(); i++) {
            ProgressPhoto photo = uploadPhoto(entry, files.get(i), nextOrder + i);
            entry.getPhotos().add(photo);
        }
        return toResponse(entry);
    }

    @Transactional
    public ProgressEntryResponse updateCaption(User user, Long entryId, String caption) {
        ProgressEntry entry = getOwnedEntry(user, entryId);
        entry.setCaption(caption != null ? caption : "");
        entryRepository.save(entry);
        return toResponse(entry);
    }

    @Transactional
    public void deleteEntry(User user, Long entryId) {
        ProgressEntry entry = getOwnedEntry(user, entryId);
        // Delete all photo files from storage
        for (ProgressPhoto photo : entry.getPhotos()) {
            fileStorageService.delete(photo.getUrl());
        }
        entryRepository.delete(entry);
    }

    @Transactional
    public void deletePhoto(User user, Long entryId, Long photoId) {
        ProgressEntry entry = getOwnedEntry(user, entryId);
        ProgressPhoto photo = photoRepository.findByIdAndEntryId(photoId, entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        fileStorageService.delete(photo.getUrl());
        entry.getPhotos().remove(photo);
        photoRepository.delete(photo);

        // If last photo, delete the entire entry
        if (entry.getPhotos().isEmpty()) {
            entryRepository.delete(entry);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private ProgressEntry getOwnedEntry(User user, Long entryId) {
        return entryRepository.findByIdAndUser(entryId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private ProgressPhoto uploadPhoto(ProgressEntry entry, MultipartFile file, int sortOrder) {
        String ext = getExtension(file.getOriginalFilename());
        String fileName = entry.getId() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        String url = fileStorageService.upload("progress-photos", fileName, file);

        ProgressPhoto photo = new ProgressPhoto();
        photo.setEntry(entry);
        photo.setUrl(url);
        photo.setSortOrder(sortOrder);
        return photoRepository.save(photo);
    }

    private ProgressEntryResponse toResponse(ProgressEntry entry) {
        List<ProgressPhotoResponse> photos = entry.getPhotos().stream()
                .map(p -> new ProgressPhotoResponse(p.getId(), p.getUrl()))
                .toList();
        return new ProgressEntryResponse(entry.getId(), entry.getDate(), entry.getCaption(), photos);
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
