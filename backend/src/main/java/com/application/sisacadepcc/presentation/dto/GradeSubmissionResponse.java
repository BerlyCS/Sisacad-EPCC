package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record GradeSubmissionResponse(
        Long groupId,
        Long courseId,
        String courseCode,
        Long studentId,
        List<Integer> continuousGrades,
        List<Integer> examGrades,
        Double finalGrade,
        String status
) {}
