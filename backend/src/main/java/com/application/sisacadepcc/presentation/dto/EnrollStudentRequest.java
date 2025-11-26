package com.application.sisacadepcc.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollStudentRequest(
        Long studentId,
        String studentCui,
        @NotNull Long courseGroupId
) {
    public boolean hasStudentIdentifier() {
        return studentId != null || (studentCui != null && !studentCui.isBlank());
    }
}
