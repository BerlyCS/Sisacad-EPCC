package com.application.sisacadepcc.presentation.dto;

public record LabEnrollmentRequest(
        Long studentId,
        String studentCui
) {
    public boolean hasExplicitIdentifier() {
        return studentId != null || (studentCui != null && !studentCui.isBlank());
    }
}
