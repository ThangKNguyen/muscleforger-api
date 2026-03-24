package com.muscleforger.api.repository;

import com.muscleforger.api.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, String> {

    List<Exercise> findByBodyPartIgnoreCase(String bodyPart);

    List<Exercise> findByTargetIgnoreCase(String target);

    List<Exercise> findByEquipmentIgnoreCase(String equipment);

    @Query("""
        SELECT e FROM Exercise e WHERE
        LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(e.target) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(e.equipment) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(e.bodyPart) LIKE LOWER(CONCAT('%', :q, '%'))
    """)
    List<Exercise> search(String q);

    @Query("SELECT DISTINCT e.bodyPart FROM Exercise e ORDER BY e.bodyPart")
    List<String> findDistinctBodyParts();

    boolean existsById(String id);
}
