package com.application.sisacadepcc.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SyllabusTopicRequest(
        @NotBlank(message = "Topic name is required")
        String name,

        @NotNull(message = "Topic weight is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Topic weight must be positive")
        Double weight,

        @NotNull(message = "The session date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate sessionDate
) {
}
