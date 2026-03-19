package com.muscleforger.api.repository;

import com.muscleforger.api.entity.TemplateDay;
import com.muscleforger.api.entity.WorkoutTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateDayRepository extends JpaRepository<TemplateDay, Long> {

    List<TemplateDay> findByTemplateOrderByDayNumberAsc(WorkoutTemplate template);

    Optional<TemplateDay> findByIdAndTemplate(Long id, WorkoutTemplate template);

    long countByTemplate(WorkoutTemplate template);
}
