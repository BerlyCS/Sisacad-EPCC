package com.application.sisacadepcc.domain.model.valueobject;

import java.util.Arrays;

public enum ExamStatisticType {
    MEAN,
    BEST,
    WORST;

    public static ExamStatisticType fromLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("summaryType is required");
        }
        String normalized = label.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(value -> value.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown summaryType: " + label));
    }
}
