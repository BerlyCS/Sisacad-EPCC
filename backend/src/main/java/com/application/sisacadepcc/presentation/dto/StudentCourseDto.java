package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.valueobject.CourseType;

public class StudentCourseDto {

    private final Long courseId;
    private final Long courseCode;
    private final String name;
    private final String groupLetter;
    private final CourseType courseType;
    private final String courseTypeLabel;
    private final Integer creditNumber;
    private final Long courseGroupId;

    public StudentCourseDto(Long courseId,
                            Long courseCode,
                            String name,
                            String groupLetter,
                            CourseType courseType,
                            String courseTypeLabel,
                            Integer creditNumber,
                            Long courseGroupId) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.name = name;
        this.groupLetter = groupLetter;
        this.courseType = courseType;
        this.courseTypeLabel = courseTypeLabel;
        this.creditNumber = creditNumber;
        this.courseGroupId = courseGroupId;
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

    public String getGroupLetter() {
        return groupLetter;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public String getCourseTypeLabel() {
        return courseTypeLabel;
    }

    public Integer getCreditNumber() {
        return creditNumber;
    }

    public Long getCourseGroupId() {
        return courseGroupId;
    }
}
