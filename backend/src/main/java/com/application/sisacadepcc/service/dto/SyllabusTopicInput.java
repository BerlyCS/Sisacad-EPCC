package com.application.sisacadepcc.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SyllabusTopicInput(
        String name,
        BigDecimal weight,
        LocalDate sessionDate
) {
}
