package com.muscleforger.api.repository;

import com.muscleforger.api.entity.ProgressPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgressPhotoRepository extends JpaRepository<ProgressPhoto, Long> {
    Optional<ProgressPhoto> findByIdAndEntryId(Long id, Long entryId);
}
