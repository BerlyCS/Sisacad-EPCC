package com.application.sisacadepcc.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollStudentRequest(
        String studentDocumentoIdentidad,
        String studentCui,
        @NotNull Long courseId
) {
    public boolean hasStudentIdentifier() {
        return (studentDocumentoIdentidad != null && !studentDocumentoIdentidad.isBlank())
                || (studentCui != null && !studentCui.isBlank());
    }
}
