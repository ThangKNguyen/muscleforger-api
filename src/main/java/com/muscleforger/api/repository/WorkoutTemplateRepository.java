package com.muscleforger.api.repository;

import com.muscleforger.api.entity.User;
import com.muscleforger.api.entity.WorkoutTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {

    List<WorkoutTemplate> findByUserOrderByUpdatedAtDesc(User user);

    Optional<WorkoutTemplate> findByIdAndUser(Long id, User user);
}
