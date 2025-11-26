package com.application.sisacadepcc.presentation.dto;

public record CourseGroupSummaryResponse(
        Long groupId,
        Long courseId,
        String courseCode,
        String courseName,
        String groupLetter,
        String courseType,
        boolean canGrade,
        int studentCount,
        int maxCapacity
) {}
