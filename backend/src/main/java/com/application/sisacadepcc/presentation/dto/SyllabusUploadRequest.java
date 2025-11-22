package com.application.sisacadepcc.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SyllabusUploadRequest(
        @NotNull(message = "Course id is required")
        Long courseId,

        @Valid
        List<SyllabusTopicRequest> topics
) {
}
