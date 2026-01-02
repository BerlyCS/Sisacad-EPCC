package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.Course;

public record CourseSummaryResponse(
    Long courseId,
    Integer courseCode,
    String name,
    Integer credits,
    Long syllabusId,
    Integer labHours,
    Integer practiceHours,
    Integer theoryHours,
    Integer semesterNumber,
    Long enrolledCount
) {
    public static CourseSummaryResponse from(Course course, Long enrolledCount) {
        if (course == null) return null;
        return new CourseSummaryResponse(
                course.getCourseId(),
                course.getCourseCode(),
                course.getName(),
                course.getCredits(),
                course.getSyllabusId(),
                course.getLabHours(),
                course.getPracticeHours(),
                course.getTheoryHours(),
                course.getSemesterNumber(),
                enrolledCount != null ? enrolledCount : 0L
        );
    }
}
