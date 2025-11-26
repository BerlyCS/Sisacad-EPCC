package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;

import java.util.List;

public record CourseGroupAssignmentResponse(
        Long groupId,
        Long courseId,
        String letter,
        CourseType type,
        String typeLabel,
        Integer maxCapacity,
        Integer availableCapacity,
        Long teacherId,
        List<CourseScheduleSlotResponse> scheduleSlots
) {
    public static CourseGroupAssignmentResponse from(CourseGroup group) {
        if (group == null) {
            return null;
        }
        String label;
        if (group.getType() == CourseType.LAB) {
            label = "Laboratorio";
        } else if (group.getType() == CourseType.PRACTICE) {
            label = "Práctica";
        } else {
            label = "Teoría";
        }
        return new CourseGroupAssignmentResponse(
                group.getId(),
                group.getCourseId(),
                group.getLetter(),
                group.getType(),
                label,
                group.getMaxCapacity(),
                group.getAvailableCapacity(),
                group.getTeacherId(),
                CourseScheduleSlotResponse.fromList(group.getCourseSchedules())
        );
    }
}
