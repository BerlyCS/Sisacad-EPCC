package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record EnrollmentResponse(
        boolean success,
        String message,
        Long studentId,
        Long courseId,
        List<Long> courseGroupIds
) {
        public static EnrollmentResponse success(Long studentId, Long courseId, List<Long> courseGroupIds) {
                return new EnrollmentResponse(true, "Estudiante matriculado exitosamente", studentId, courseId, courseGroupIds);
        }

        public static EnrollmentResponse failure(String message, Long studentId, Long courseId, List<Long> courseGroupIds) {
                return new EnrollmentResponse(false, message, studentId, courseId, courseGroupIds);
        }
}
