package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.ClassType;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance captured for an individual student, inheriting shared metadata
 * from {@link Attendance} while keeping a reference to the professor session
 * it belongs to.
 */
public final class StudentAttendance extends Attendance {

    private final Long sessionId;
    private final String studentId;

    public StudentAttendance(Long attendanceId,
                             Long sessionId,
                             String studentId,
                             AttendanceStatus status,
                             LocalDateTime timestamp,
                             GeoLocation location,
                             LocalDate date,
                             Long courseId,
                             Long courseGroupId,
                             ClassType classType,
                             String notes) {
        super(attendanceId, courseId, courseGroupId, status, timestamp, location, date, classType, notes);
        this.sessionId = sessionId;
        this.studentId = studentId;
    }

    public StudentAttendance(Long attendanceId,
                             Long sessionId,
                             String studentId,
                             AttendanceStatus status) {
        this(attendanceId, sessionId, studentId, status, null, null, null, null, null, null, null);
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getStudentId() {
        return studentId;
    }

    public static StudentAttendance forSession(Long sessionId,
                                               String studentId,
                                               AttendanceStatus status) {
        return new StudentAttendance(null, sessionId, studentId, status);
    }

    public static StudentAttendance pendingFor(String studentId,
                                               AttendanceStatus status) {
        return new StudentAttendance(null, null, studentId, status);
    }
}
