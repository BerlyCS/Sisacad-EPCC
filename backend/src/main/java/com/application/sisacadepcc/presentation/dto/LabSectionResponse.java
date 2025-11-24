package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;

import java.util.List;

public record LabSectionResponse(
        Long courseId,
        String name,
        String groupLetter,
        Integer labCapacity,
        Integer enrolledCount,
        Integer remainingSeats,
        Long theoryCourseId,
        String courseTypeLabel,
        List<Schedule> scheduleSlots
) {
    public static LabSectionResponse from(CourseGroup labGroup, long enrolledCount) {
        Course course = labGroup.getCourse();
        int capacity = labGroup.getMaxCapacity();
        int remaining = Math.max(capacity - (int) enrolledCount, 0);
        String typeLabel = mapCourseType(labGroup.getType());

        String groupLetter = labGroup.getLetter() != null ? labGroup.getLetter() : null;

        return new LabSectionResponse(
            course.getCourseId(),
            course.getName(),
            groupLetter,
                capacity,
                (int) enrolledCount,
                remaining,
                null,
                typeLabel,
                labGroup.getScheduleSlots()
        );
    }

    private static String mapCourseType(CourseType courseType) {
        if (courseType == null) {
            return "Laboratorio";
        }

        return switch (courseType) {
            case THEORY -> "Teoría";
            case LAB -> "Laboratorio";
            default -> "Laboratorio";
        };
    }
}
