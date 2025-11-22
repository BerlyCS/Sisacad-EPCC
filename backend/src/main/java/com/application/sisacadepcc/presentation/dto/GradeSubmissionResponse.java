package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record GradeSubmissionResponse(
        Long courseId,
        String courseCode,
        String studentDocumentoIdentidad,
        List<Integer> continuousGrades,
        List<Integer> examGrades,
        Double finalGrade,
        String status
) {}
