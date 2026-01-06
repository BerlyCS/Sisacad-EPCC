package com.application.sisacadepcc.service.dto;

import java.util.Collections;
import java.util.List;

public record UserImportResult<T>(
        List<T> created,
        List<String> errors,
        int processedRows,
        int skippedRows) {

    @Override
    public List<T> created() {
        return created == null ? List.of() : Collections.unmodifiableList(created);
    }

    @Override
    public List<String> errors() {
        return errors == null ? List.of() : Collections.unmodifiableList(errors);
    }

    public int importedCount() {
        return created().size();
    }
}
