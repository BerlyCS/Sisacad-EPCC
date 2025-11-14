package com.application.sisacadepcc.presentation.dto;

public record EnrollmentResponse(
        boolean success,
        String message,
        String studentDocumentoIdentidad,
        Long courseId
) {
    public static EnrollmentResponse success(String documentId, Long courseId) {
        return new EnrollmentResponse(true, "Estudiante matriculado exitosamente", documentId, courseId);
    }

    public static EnrollmentResponse failure(String message, String documentId, Long courseId) {
        return new EnrollmentResponse(false, message, documentId, courseId);
    }
}
