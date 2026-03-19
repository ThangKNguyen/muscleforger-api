package com.muscleforger.api.repository;

import com.muscleforger.api.entity.CustomExercise;
import com.muscleforger.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomExerciseRepository extends JpaRepository<CustomExercise, Long> {

    List<CustomExercise> findByUserOrderByCreatedAtDesc(User user);

    Optional<CustomExercise> findByIdAndUser(Long id, User user);

    @Query("""
            SELECT e FROM CustomExercise e
            WHERE e.user = :user
            AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(e.bodyPart) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(e.target) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(e.equipment) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY e.createdAt DESC
            """)
    List<CustomExercise> search(@Param("user") User user, @Param("q") String q);
}
