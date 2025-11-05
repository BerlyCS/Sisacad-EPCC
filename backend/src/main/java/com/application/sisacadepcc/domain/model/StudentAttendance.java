package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentAttendance {

    private final Long attendanceId;
    private final String studentId;
    private final Long courseId; // optional
    private final Long groupId;  // optional
    private final AttendanceStatus status;
    private final LocalDateTime timestamp;
    private final GeoLocation location; // optional
    private final LocalDate date;

    public StudentAttendance(Long attendanceId,
                             String studentId,
                             Long courseId,
                             Long groupId,
                             AttendanceStatus status,
                             LocalDateTime timestamp,
                             GeoLocation location,
                             LocalDate date) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.groupId = groupId;
        this.status = status;
        this.timestamp = timestamp;
        this.location = location;
        this.date = date;
    }

    public Long getAttendanceId() { return attendanceId; }
    public String getStudentId() { return studentId; }
    public Long getCourseId() { return courseId; }
    public Long getGroupId() { return groupId; }
    public AttendanceStatus getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public GeoLocation getLocation() { return location; }
    public LocalDate getDate() { return date; }
}
