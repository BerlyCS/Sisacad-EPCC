package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.ClassType;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base aggregate for any attendance record (professor, student, etc.).
 * Contains contextual information for the session while allowing
 * child classes to provide actor-specific details.
 */
public abstract class Attendance {

    private final Long attendanceId;
    private final Long courseId;
    private final Long courseGroupId;
    private final AttendanceStatus status;
    private final LocalDateTime timestamp;
    private final GeoLocation location;
    private final LocalDate date;
    private final ClassType classType;
    private final String notes;

    protected Attendance(Long attendanceId,
                         Long courseId,
                         Long courseGroupId,
                         AttendanceStatus status,
                         LocalDateTime timestamp,
                         GeoLocation location,
                         LocalDate date,
                         ClassType classType,
                         String notes) {
        this.attendanceId = attendanceId;
        this.courseId = courseId;
        this.courseGroupId = courseGroupId;
        this.status = status;
        this.timestamp = timestamp;
        this.location = location;
        this.date = date;
        this.classType = classType;
        this.notes = notes;
    }

    public Long getAttendanceId() { return attendanceId; }
    public Long getCourseId() { return courseId; }
    public Long getCourseGroupId() { return courseGroupId; }
    public AttendanceStatus getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public GeoLocation getLocation() { return location; }
    public LocalDate getDate() { return date; }
    public ClassType getClassType() { return classType; }
    public String getNotes() { return notes; }
}
