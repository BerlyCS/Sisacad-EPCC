package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record StudentGradeSubmissionRequest(
        Long studentId,
        List<Integer> continuousGrades,
        List<Integer> examGrades,
        String status,
        String feedback) {
}
