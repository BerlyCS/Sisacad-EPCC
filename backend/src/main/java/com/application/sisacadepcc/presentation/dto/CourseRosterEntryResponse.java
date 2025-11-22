package com.application.sisacadepcc.presentation.dto;

public record CourseRosterEntryResponse(
        String studentDocumentoIdentidad,
        String studentCui,
        String fullName,
        String email,
        Long courseId,
        String courseCode,
        String groupLetter,
        String courseType,
        boolean canGrade,
        java.util.List<Integer> continuousGrades,
        java.util.List<Integer> examGrades,
        Double finalGrade,
        String submissionStatus
) {}
