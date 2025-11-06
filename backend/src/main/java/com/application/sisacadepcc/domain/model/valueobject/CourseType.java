package com.application.sisacadepcc.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

/**
 * Defines the nature of a course so we can distinguish regular theoretical courses
 * from laboratory sessions that require a linked theory enrollment.
 */
public enum CourseType {
    THEORY("Teoría"),
    LAB("Laboratorio");

    private final String label;

    CourseType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static CourseType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value) || type.label.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported course type: " + value));
    }
}
