package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public record CourseRosterPageResponse(
        List<CourseRosterEntryResponse> students,
        int total,
        int page,
        int size
) {}
