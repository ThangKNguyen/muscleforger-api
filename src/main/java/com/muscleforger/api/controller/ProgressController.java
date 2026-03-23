package com.muscleforger.api.controller;

import com.muscleforger.api.dto.progress.ProgressEntryResponse;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.repository.UserRepository;
import com.muscleforger.api.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress-photos")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    @GetMapping
    public List<ProgressEntryResponse> getEntries(
            Authentication auth,
            @RequestParam(required = false) Integer year) {
        return progressService.getEntries(getUser(auth), year);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProgressEntryResponse createEntry(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "") String caption,
            @RequestParam("files") List<MultipartFile> files) {
        return progressService.createEntry(getUser(auth), date, caption, files);
    }

    @PostMapping(value = "/{entryId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProgressEntryResponse addPhotos(
            Authentication auth,
            @PathVariable Long entryId,
            @RequestParam("files") List<MultipartFile> files) {
        return progressService.addPhotos(getUser(auth), entryId, files);
    }

    @PutMapping("/{entryId}")
    public ProgressEntryResponse updateCaption(
            Authentication auth,
            @PathVariable Long entryId,
            @RequestBody Map<String, String> body) {
        return progressService.updateCaption(getUser(auth), entryId, body.get("caption"));
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(Authentication auth, @PathVariable Long entryId) {
        progressService.deleteEntry(getUser(auth), entryId);
    }

    @DeleteMapping("/{entryId}/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto(Authentication auth,
                            @PathVariable Long entryId,
                            @PathVariable Long photoId) {
        progressService.deletePhoto(getUser(auth), entryId, photoId);
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
