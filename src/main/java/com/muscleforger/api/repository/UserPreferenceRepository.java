package com.muscleforger.api.repository;

import com.muscleforger.api.entity.User;
import com.muscleforger.api.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUser(User user);
}
