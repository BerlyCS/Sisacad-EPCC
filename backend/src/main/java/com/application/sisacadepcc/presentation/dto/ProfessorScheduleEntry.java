package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.valueobject.CourseType;

/**
 * Represents a weekly schedule block assigned to a professor.
 */
public class ProfessorScheduleEntry {

    private final Long courseId;
    private final Long courseCode;
    private final String courseName;
    private final String groupLetter;
    private final CourseType courseType;
    private final String dayOfWeek;
    private final String startTime;
    private final String endTime;
    private final String classroomName;

    public ProfessorScheduleEntry(Long courseId,
                                  Long courseCode,
                                  String courseName,
                                  String groupLetter,
                                  CourseType courseType,
                                  String dayOfWeek,
                                  String startTime,
                                  String endTime,
                                  String classroomName) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.groupLetter = groupLetter;
        this.courseType = courseType;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroomName = classroomName;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getGroupLetter() {
        return groupLetter;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getClassroomName() {
        return classroomName;
    }
}
