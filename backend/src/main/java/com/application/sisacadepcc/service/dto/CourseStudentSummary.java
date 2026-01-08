package com.application.sisacadepcc.service.dto;

public record CourseStudentSummary(
        Long studentId,
        String cui,
        String firstNames,
        String paternalSurname,
        String maternalSurname,
        String institutionalEmail,
        Long courseId,
        Long groupId,
        String groupLetter
) {
}
