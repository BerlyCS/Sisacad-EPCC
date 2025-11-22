package com.application.sisacadepcc.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SyllabusTopicsUpdateRequest(
        @NotNull(message = "Course id is required")
        Long courseId,

        @Valid
        List<SyllabusTopicRequest> topics
) {
}
