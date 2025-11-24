package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.ClassType;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Attendance {

    private final Long attendanceId;
    private final Long professorId;
    private final Long courseId; // optional
    private final Long courseGroupId;  // optional
    private final AttendanceStatus status;
    private final LocalDateTime timestamp;
    private final GeoLocation location; // optional
    private final LocalDate date;
    private final ClassType classType;
    private final String todo;

    public Attendance(Long attendanceId,
                      Long professorId,
                      Long courseId,
                      Long courseGroupId,
                      AttendanceStatus status,
                      LocalDateTime timestamp,
                      GeoLocation location,
                      LocalDate date,
                      ClassType classType,
                      String todo) {
        this.attendanceId = attendanceId;
        this.professorId = professorId;
        this.courseId = courseId;
        this.courseGroupId = courseGroupId;
        this.status = status;
        this.timestamp = timestamp;
        this.location = location;
        this.date = date;
        this.classType = classType;
        this.todo = todo;
    }

    public Long getAttendanceId() { return attendanceId; }
    public Long getProfessorId() { return professorId; }
    public Long getCourseId() { return courseId; }
    public Long getCourseGroupId() { return courseGroupId; }
    public AttendanceStatus getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public GeoLocation getLocation() { return location; }
    public LocalDate getDate() { return date; }
    public ClassType getClassType() { return classType; }
    public String getTodo() { return todo; }
}
