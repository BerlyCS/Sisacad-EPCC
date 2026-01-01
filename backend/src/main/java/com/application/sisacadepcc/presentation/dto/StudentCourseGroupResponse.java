package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.valueobject.CourseType;

public class StudentCourseGroupResponse {

    private final Long courseGroupId;
    private final String groupLetter;
    private final CourseType courseType;
    private final String courseTypeLabel;

    public StudentCourseGroupResponse(Long courseGroupId,
                                      String groupLetter,
                                      CourseType courseType,
                                      String courseTypeLabel) {
        this.courseGroupId = courseGroupId;
        this.groupLetter = groupLetter;
        this.courseType = courseType;
        this.courseTypeLabel = courseTypeLabel;
    }

    public Long getCourseGroupId() {
        return courseGroupId;
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
}
