package com.application.sisacadepcc.presentation.dto;

public record EnrollmentResponse(
        boolean success,
        String message,
        Long studentId,
        Long courseId
) {
        public static EnrollmentResponse success(Long studentId, Long courseId) {
                return new EnrollmentResponse(true, "Estudiante matriculado exitosamente", studentId, courseId);
        }

        public static EnrollmentResponse failure(String message, Long studentId, Long courseId) {
                return new EnrollmentResponse(false, message, studentId, courseId);
        }
}
