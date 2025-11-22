package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record CreateLabSectionRequest(
        Long theoryCourseId,
        String name,
        String groupLetter,
        Integer labCapacity,
        List<LabScheduleSlotDto> scheduleSlots
) {
}
