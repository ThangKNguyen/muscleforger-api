package com.muscleforger.api.service;

import com.muscleforger.api.dto.settings.*;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.entity.UserPreference;
import com.muscleforger.api.repository.UserPreferenceRepository;
import com.muscleforger.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserProfileResponse getProfile(User user) {
        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getAvatarUrl(), user.getCreatedAt());
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.username() != null) {
            if (!request.username().equals(user.getUsername())
                    && userRepository.existsByUsername(request.username())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
            }
            user.setUsername(request.username());
        }
        if (request.email() != null) {
            if (!request.email().equals(user.getEmail())
                    && userRepository.existsByEmail(request.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
            }
            user.setEmail(request.email());
        }
        userRepository.save(user);
        return getProfile(user);
    }

    @Transactional
    public String uploadAvatar(User user, MultipartFile file) {
        // Delete old avatar if exists
        if (user.getAvatarUrl() != null) {
            fileStorageService.delete(user.getAvatarUrl());
        }

        String ext = getExtension(file.getOriginalFilename());
        String fileName = user.getId() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        String url = fileStorageService.upload("avatars", fileName, file);

        user.setAvatarUrl(url);
        userRepository.save(user);
        return url;
    }

    @Transactional
    public void deleteAvatar(User user) {
        if (user.getAvatarUrl() != null) {
            fileStorageService.delete(user.getAvatarUrl());
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(User user, DeleteAccountRequest request) {
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is incorrect");
        }
        // Delete avatar file if exists
        if (user.getAvatarUrl() != null) {
            fileStorageService.delete(user.getAvatarUrl());
        }
        userRepository.delete(user);
    }

    // ── Preferences ─────────────────────────────────────────────────────────────

    public PreferencesResponse getPreferences(User user) {
        UserPreference pref = preferenceRepository.findByUser(user)
                .orElseGet(() -> {
                    UserPreference p = new UserPreference();
                    p.setUser(user);
                    return preferenceRepository.save(p);
                });
        return new PreferencesResponse(pref.getTheme(), pref.getWeightUnit());
    }

    @Transactional
    public PreferencesResponse updatePreferences(User user, UpdatePreferencesRequest request) {
        UserPreference pref = preferenceRepository.findByUser(user)
                .orElseGet(() -> {
                    UserPreference p = new UserPreference();
                    p.setUser(user);
                    return p;
                });
        if (request.theme() != null) pref.setTheme(request.theme());
        if (request.weightUnit() != null) pref.setWeightUnit(request.weightUnit());
        preferenceRepository.save(pref);
        return new PreferencesResponse(pref.getTheme(), pref.getWeightUnit());
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
