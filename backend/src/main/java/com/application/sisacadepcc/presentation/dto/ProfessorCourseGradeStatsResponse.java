package com.application.sisacadepcc.presentation.dto;

public record ProfessorCourseGradeStatsResponse(
        String courseCode,
        Double meanGrade,
        Double highestGrade,
        Double lowestGrade,
        Double meanFinalGrade,
        Double highestFinalGrade,
        Double lowestFinalGrade,
        int gradedStudents
) {
}
