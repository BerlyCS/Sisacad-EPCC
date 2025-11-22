package com.application.sisacadepcc.presentation.dto;

public record LabScheduleSlotDto(
        String classroomName,
        String dayOfWeek,
        String startTime,
        String endTime
) {
}
