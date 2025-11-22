package com.application.sisacadepcc.presentation.dto;

public record LabSlotSuggestionResponse(
        String classroomName,
        String dayOfWeek,
        String startTime,
        String endTime,
        String groupLetter,
        String courseName
) {
}
