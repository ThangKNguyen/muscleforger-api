package com.muscleforger.api.repository;

import com.muscleforger.api.entity.TemplateDay;
import com.muscleforger.api.entity.TemplateExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateExerciseRepository extends JpaRepository<TemplateExercise, Long> {

    List<TemplateExercise> findByDayOrderByPositionAsc(TemplateDay day);

    Optional<TemplateExercise> findByIdAndDay(Long id, TemplateDay day);

    @Query("SELECT MAX(e.position) FROM TemplateExercise e WHERE e.day = :day")
    Integer findMaxPositionByDay(@Param("day") TemplateDay day);
}
