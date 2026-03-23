package com.muscleforger.api.dto.progress;

import java.time.LocalDate;
import java.util.List;

public record ProgressEntryResponse(
        Long id,
        LocalDate date,
        String caption,
        List<ProgressPhotoResponse> photos
) {}
