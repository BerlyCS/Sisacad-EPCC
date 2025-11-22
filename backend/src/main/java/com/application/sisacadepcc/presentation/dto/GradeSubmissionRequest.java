package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record GradeSubmissionRequest(
        List<Integer> continuousGrades,
        List<Integer> examGrades,
        String status,
        String feedback
) {}
