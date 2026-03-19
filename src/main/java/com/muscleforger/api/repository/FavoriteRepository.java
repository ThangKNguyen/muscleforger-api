package com.muscleforger.api.repository;

import com.muscleforger.api.entity.Favorite;
import com.muscleforger.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
    boolean existsByUserAndExerciseId(User user, String exerciseId);
    void deleteByUserAndExerciseId(User user, String exerciseId);
}
