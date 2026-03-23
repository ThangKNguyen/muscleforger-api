package com.muscleforger.api.repository;

import com.muscleforger.api.entity.User;
import com.muscleforger.api.entity.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {
    List<WeightLog> findByUserOrderByDateAsc(User user);
    List<WeightLog> findByUserAndDateBetweenOrderByDateAsc(User user, LocalDate from, LocalDate to);
    boolean existsByUserAndDate(User user, LocalDate date);
}
