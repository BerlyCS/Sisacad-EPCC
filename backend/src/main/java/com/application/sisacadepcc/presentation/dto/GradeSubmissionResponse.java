package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record GradeSubmissionResponse(
        Long groupId,
        Long courseId,
        String courseCode,
        Long studentUserId,
        List<Integer> continuousGrades,
        List<Integer> examGrades,
        Double finalGrade,
                String status,
                String error
) {
        public static GradeSubmissionResponse success(Long groupId,
                                                                                                   Long courseId,
                                                                                                   String courseCode,
                                                                                                   Long studentUserId,
                                                                                                   List<Integer> continuousGrades,
                                                                                                   List<Integer> examGrades,
                                                                                                   Double finalGrade,
                                                                                                   String status) {
                return new GradeSubmissionResponse(groupId, courseId, courseCode, studentUserId, continuousGrades, examGrades, finalGrade, status, null);
        }

        public static GradeSubmissionResponse error(String message) {
                return new GradeSubmissionResponse(null, null, null, null, List.of(), List.of(), null, "ERROR", message);
        }
}
