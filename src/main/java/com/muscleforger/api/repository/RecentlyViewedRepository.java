package com.muscleforger.api.repository;

import com.muscleforger.api.entity.RecentlyViewed;
import com.muscleforger.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewed, Long> {
    List<RecentlyViewed> findByUserOrderByViewedAtDesc(User user);
    Optional<RecentlyViewed> findByUserAndExerciseId(User user, String exerciseId);
}
