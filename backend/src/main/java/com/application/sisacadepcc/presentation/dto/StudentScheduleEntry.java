package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.valueobject.CourseType;

public class StudentScheduleEntry {
    private final Long courseId;
    private final Long courseCode;
    private final String courseName;
    private final CourseType courseType;
    private final String dayOfWeek;
    private final String startTime;
    private final String endTime;
    private final String classroomName;

    public StudentScheduleEntry(Long courseId,
                                 Long courseCode,
                                 String courseName,
                                 CourseType courseType,
                                 String dayOfWeek,
                                 String startTime,
                                 String endTime,
                                 String classroomName) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.courseType = courseType;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroomName = classroomName;
    }

    public Long getCourseId() { return courseId; }
    public Long getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public CourseType getCourseType() { return courseType; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getClassroomName() { return classroomName; }
}
