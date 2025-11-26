package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.CourseGroupExamsPdf;

public record ExamSummaryResponse(
        Long summaryId,
        Long groupId,
        int examNumber,
        String summaryType,
        String fileName,
        Long fileSizeBytes
) {
    public static ExamSummaryResponse from(CourseGroupExamsPdf summary) {
        return new ExamSummaryResponse(
                summary.getId(),
                summary.getCourseGroupId(),
                summary.getExamNumber(),
                summary.getSummaryType() != null ? summary.getSummaryType().name() : null,
                summary.getContent() != null ? summary.getContent().getName() : null,
                summary.getContent() != null ? summary.getContent().getSizeBytes() : null
        );
    }
}
