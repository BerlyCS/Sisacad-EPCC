package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record CreateCourseGroupRequest(
        String letter,
        String type,
        Integer capacity,
        List<CourseScheduleSlotRequest> scheduleSlots
) {
}
