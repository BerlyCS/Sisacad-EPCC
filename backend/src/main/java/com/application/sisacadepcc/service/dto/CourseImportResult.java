package com.application.sisacadepcc.service.dto;

import com.application.sisacadepcc.domain.model.Course;

import java.util.Collections;
import java.util.List;

public record CourseImportResult(
        List<Course> createdCourses,
        List<String> errors,
        int processedRows,
        int skippedRows) {

    @Override
    public List<Course> createdCourses() {
        return createdCourses == null ? List.of() : Collections.unmodifiableList(createdCourses);
    }

    @Override
    public List<String> errors() {
        return errors == null ? List.of() : Collections.unmodifiableList(errors);
    }

    public int importedCount() {
        return createdCourses().size();
    }
}
