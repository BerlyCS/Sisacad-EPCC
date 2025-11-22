package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record UpdateLabSectionRequest(
        String name,
        String groupLetter,
        Integer labCapacity,
        List<LabScheduleSlotDto> scheduleSlots
) {
}
