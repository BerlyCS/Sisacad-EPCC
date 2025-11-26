package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.valueobject.ExamStatisticType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ExamSummaryUploadRequest(
        @Min(1) @Max(3) int examNumber,
        @NotBlank String summaryType
) {
    public ExamStatisticType resolveSummaryType() {
        return ExamStatisticType.fromLabel(summaryType);
    }
}
