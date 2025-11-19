package com.application.sisacadepcc.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentGradeResponse(
        String courseCode,
        Long courseId,
        String courseName,
        List<Integer> continuousGrades,
        List<Integer> examGrades,
        List<BigDecimal> continuousWeights,
        List<BigDecimal> examWeights,
        BigDecimal finalGrade
) {
}
