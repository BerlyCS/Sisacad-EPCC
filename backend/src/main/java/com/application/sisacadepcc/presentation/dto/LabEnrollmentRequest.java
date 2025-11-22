package com.application.sisacadepcc.presentation.dto;

public record LabEnrollmentRequest(
        String studentDocumentoIdentidad,
        String studentCui
) {
    public boolean hasExplicitIdentifier() {
        return (studentDocumentoIdentidad != null && !studentDocumentoIdentidad.isBlank())
                || (studentCui != null && !studentCui.isBlank());
    }
}
