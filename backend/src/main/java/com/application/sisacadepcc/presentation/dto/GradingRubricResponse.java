package com.application.sisacadepcc.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public record GradingRubricResponse(
        Long courseId,
        String courseCode,
        List<BigDecimal> continuousWeights,
        List<BigDecimal> examWeights
) {}
