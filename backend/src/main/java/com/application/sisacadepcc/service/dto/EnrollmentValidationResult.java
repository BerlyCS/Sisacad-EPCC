package com.application.sisacadepcc.service.dto;

public record EnrollmentValidationResult(
        boolean allowed,
        String errorCode,
        String message,
        Long courseId,
        Integer remainingSeats
) {
    public static EnrollmentValidationResult success(Long courseId, Integer remainingSeats) {
        return new EnrollmentValidationResult(true, null, "Solicitud válida", courseId, remainingSeats);
    }

    public static EnrollmentValidationResult failure(String errorCode, String message, Long courseId, Integer remainingSeats) {
        return new EnrollmentValidationResult(false, errorCode, message, courseId, remainingSeats);
    }
}
