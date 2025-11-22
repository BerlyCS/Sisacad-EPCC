package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.valueobject.CourseScheduleSlot;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;

import java.util.List;
import java.util.Optional;

public record LabSectionResponse(
        Long courseId,
        String name,
        String groupLetter,
        Integer labCapacity,
        Integer enrolledCount,
        Integer remainingSeats,
        Long theoryCourseId,
        String courseTypeLabel,
        List<CourseScheduleSlot> scheduleSlots
) {
    public static LabSectionResponse from(Course labCourse, long enrolledCount) {
        int capacity = Optional.ofNullable(labCourse.getLabCapacity()).orElse(labCourse.getEffectiveLabCapacity());
        int remaining = Math.max(capacity - (int) enrolledCount, 0);
        String typeLabel = mapCourseType(labCourse.getCourseType());

        String groupLetter = labCourse.getGroupLetter() == 0
            ? null
            : String.valueOf(labCourse.getGroupLetter());

        return new LabSectionResponse(
            labCourse.getCourseId(),
            labCourse.getName(),
            groupLetter,
                capacity,
                (int) enrolledCount,
                remaining,
                labCourse.getLabPrerequisiteCourseId(),
                typeLabel,
                labCourse.getScheduleSlots()
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
