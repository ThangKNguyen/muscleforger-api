package com.muscleforger.api.service;

import com.muscleforger.api.entity.Favorite;
import com.muscleforger.api.entity.RecentlyViewed;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.entity.WorkoutExercise;
import com.muscleforger.api.repository.FavoriteRepository;
import com.muscleforger.api.repository.RecentlyViewedRepository;
import com.muscleforger.api.repository.WorkoutExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDataService {

    private final FavoriteRepository favoriteRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final RecentlyViewedRepository recentlyViewedRepository;
    private final ExerciseService exerciseService;

    private static final int HISTORY_CAP = 20;

    // ── Favorites ──────────────────────────────────────────────────────────────

    public List<Object> getFavorites(User user) {
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(f -> exerciseService.getExerciseById(f.getExerciseId()))
                .toList();
    }

    @Transactional
    public void addFavorite(User user, String exerciseId) {
        if (favoriteRepository.existsByUserAndExerciseId(user, exerciseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already in favorites");
        }
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setExerciseId(exerciseId);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(User user, String exerciseId) {
        favoriteRepository.deleteByUserAndExerciseId(user, exerciseId);
    }

    // ── Workout ────────────────────────────────────────────────────────────────

    public List<Object> getWorkout(User user) {
        return workoutExerciseRepository.findByUserOrderByAddedAtAsc(user)
                .stream()
                .map(w -> exerciseService.getExerciseById(w.getExerciseId()))
                .toList();
    }

    @Transactional
    public void addToWorkout(User user, String exerciseId) {
        if (workoutExerciseRepository.existsByUserAndExerciseId(user, exerciseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already in workout");
        }
        WorkoutExercise entry = new WorkoutExercise();
        entry.setUser(user);
        entry.setExerciseId(exerciseId);
        workoutExerciseRepository.save(entry);
    }

    @Transactional
    public void removeFromWorkout(User user, String exerciseId) {
        workoutExerciseRepository.deleteByUserAndExerciseId(user, exerciseId);
    }

    @Transactional
    public void clearWorkout(User user) {
        workoutExerciseRepository.deleteByUser(user);
    }

    // ── History ────────────────────────────────────────────────────────────────

    public List<Object> getHistory(User user) {
        return recentlyViewedRepository.findByUserOrderByViewedAtDesc(user)
                .stream()
                .map(r -> exerciseService.getExerciseById(r.getExerciseId()))
                .toList();
    }

    @Transactional
    public void logView(User user, String exerciseId) {
        // Upsert — update viewed_at if exists, insert if not
        RecentlyViewed entry = recentlyViewedRepository
                .findByUserAndExerciseId(user, exerciseId)
                .orElseGet(() -> {
                    RecentlyViewed rv = new RecentlyViewed();
                    rv.setUser(user);
                    rv.setExerciseId(exerciseId);
                    return rv;
                });
        entry.setViewedAt(LocalDateTime.now());
        recentlyViewedRepository.save(entry);

        // Cap at 20 — drop oldest entries beyond the limit
        List<RecentlyViewed> all = recentlyViewedRepository.findByUserOrderByViewedAtDesc(user);
        if (all.size() > HISTORY_CAP) {
            recentlyViewedRepository.deleteAll(all.subList(HISTORY_CAP, all.size()));
        }
    }
}
