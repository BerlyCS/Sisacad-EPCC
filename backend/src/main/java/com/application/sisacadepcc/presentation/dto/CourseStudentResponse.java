package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.service.dto.CourseStudentSummary;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CourseStudentResponse(
        Long studentId,
        String cui,
        String fullName,
        String email,
        Long groupId,
        String groupLetter
) {

    public static CourseStudentResponse from(CourseStudentSummary summary) {
        if (summary == null) {
            return null;
        }
        return new CourseStudentResponse(
                summary.studentId(),
                summary.cui(),
                buildFullName(summary),
                summary.institutionalEmail(),
                summary.groupId(),
                summary.groupLetter()
        );
    }

    private static String buildFullName(CourseStudentSummary summary) {
        return Stream.of(summary.firstNames(), summary.paternalSurname(), summary.maternalSurname())
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(" "));
    }
}
