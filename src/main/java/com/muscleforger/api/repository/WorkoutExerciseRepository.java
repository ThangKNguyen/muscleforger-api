package com.muscleforger.api.repository;

import com.muscleforger.api.entity.User;
import com.muscleforger.api.entity.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
    List<WorkoutExercise> findByUserOrderByAddedAtAsc(User user);
    boolean existsByUserAndExerciseId(User user, String exerciseId);
    void deleteByUserAndExerciseId(User user, String exerciseId);
    void deleteByUser(User user);
}
