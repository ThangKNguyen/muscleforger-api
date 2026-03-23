package com.muscleforger.api.repository;

import com.muscleforger.api.entity.ProgressEntry;
import com.muscleforger.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProgressEntryRepository extends JpaRepository<ProgressEntry, Long> {
    List<ProgressEntry> findByUserOrderByDateDesc(User user);

    @Query("SELECT e FROM ProgressEntry e WHERE e.user = :user AND YEAR(e.date) = :year ORDER BY e.date DESC")
    List<ProgressEntry> findByUserAndYear(User user, int year);

    Optional<ProgressEntry> findByIdAndUser(Long id, User user);
}
