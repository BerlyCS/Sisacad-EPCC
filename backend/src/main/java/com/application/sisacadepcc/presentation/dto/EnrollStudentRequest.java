package com.application.sisacadepcc.presentation.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record EnrollStudentRequest(
        Long studentId,
        String studentCui,
        List<Long> courseGroupIds
) {
    public boolean hasStudentIdentifier() {
        return studentId != null || (studentCui != null && !studentCui.isBlank());
    }

    public List<Long> sanitizedCourseGroupIds() {
        if (courseGroupIds == null || courseGroupIds.isEmpty()) {
            return Collections.emptyList();
        }
        return courseGroupIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public boolean hasCourseGroups() {
        return !sanitizedCourseGroupIds().isEmpty();
    }
}
