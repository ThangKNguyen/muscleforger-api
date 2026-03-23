package com.muscleforger.api.controller;

import com.muscleforger.api.dto.settings.*;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.repository.UserRepository;
import com.muscleforger.api.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService settingsService;
    private final UserRepository userRepository;

    @GetMapping
    public UserProfileResponse getProfile(Authentication auth) {
        return settingsService.getProfile(getUser(auth));
    }

    @PutMapping
    public UserProfileResponse updateProfile(Authentication auth,
                                             @Valid @RequestBody UpdateProfileRequest request) {
        return settingsService.updateProfile(getUser(auth), request);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadAvatar(Authentication auth,
                                            @RequestParam("file") MultipartFile file) {
        String url = settingsService.uploadAvatar(getUser(auth), file);
        return Map.of("avatarUrl", url);
    }

    @DeleteMapping("/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvatar(Authentication auth) {
        settingsService.deleteAvatar(getUser(auth));
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(Authentication auth,
                               @Valid @RequestBody ChangePasswordRequest request) {
        settingsService.changePassword(getUser(auth), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Authentication auth,
                              @Valid @RequestBody DeleteAccountRequest request) {
        settingsService.deleteAccount(getUser(auth), request);
    }

    // ── Preferences ─────────────────────────────────────────────────────────────

    @GetMapping("/preferences")
    public PreferencesResponse getPreferences(Authentication auth) {
        return settingsService.getPreferences(getUser(auth));
    }

    @PutMapping("/preferences")
    public PreferencesResponse updatePreferences(Authentication auth,
                                                 @Valid @RequestBody UpdatePreferencesRequest request) {
        return settingsService.updatePreferences(getUser(auth), request);
    }

    // ── Helper ──────────────────────────────────────────────────────────────────

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
