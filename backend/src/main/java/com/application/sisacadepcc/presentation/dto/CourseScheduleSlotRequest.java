package com.application.sisacadepcc.presentation.dto;

public record CourseScheduleSlotRequest(
        String dayOfWeek,
        String startTime,
        String endTime,
        Long classroomId
) {
}
