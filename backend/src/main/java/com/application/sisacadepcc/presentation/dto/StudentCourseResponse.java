package com.application.sisacadepcc.presentation.dto;

import java.util.List;

public class StudentCourseResponse {

    private final Long courseId;
    private final Long courseCode;
    private final String name;
    private final Integer creditNumber;
    private final List<StudentCourseGroupResponse> groups;

    public StudentCourseResponse(Long courseId,
                                 Long courseCode,
                                 String name,
                                 Integer creditNumber,
                                 List<StudentCourseGroupResponse> groups) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.name = name;
        this.creditNumber = creditNumber;
        this.groups = groups;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getCourseCode() {
        return courseCode;
    }

    public String getName() {
        return name;
    }

    public Integer getCreditNumber() {
        return creditNumber;
    }

    public List<StudentCourseGroupResponse> getGroups() {
        return groups;
    }
}
