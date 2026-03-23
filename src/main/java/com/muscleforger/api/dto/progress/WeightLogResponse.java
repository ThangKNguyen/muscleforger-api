package com.muscleforger.api.dto.progress;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeightLogResponse(Long id, BigDecimal weight, LocalDate date) {}
