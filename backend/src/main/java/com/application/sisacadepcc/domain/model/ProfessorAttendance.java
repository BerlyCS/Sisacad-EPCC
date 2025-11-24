package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.ClassType;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance record captured for professors, enriched with geolocation metadata.
 */
public final class ProfessorAttendance extends Attendance {

    private final Long professorId;

    public ProfessorAttendance(Long attendanceId,
                               Long professorId,
                               Long courseId,
                               Long courseGroupId,
                               AttendanceStatus status,
                               LocalDateTime timestamp,
                               GeoLocation location,
                               LocalDate date,
                               ClassType classType,
                               String notes) {
        super(attendanceId, courseId, courseGroupId, status, timestamp, location, date, classType, notes);
        this.professorId = professorId;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public static ProfessorAttendance newSession(Long professorId,
                                                 Long courseId,
                                                 Long courseGroupId,
                                                 AttendanceStatus status,
                                                 GeoLocation location,
                                                 LocalDateTime timestamp,
                                                 LocalDate date,
                                                 ClassType classType,
                                                 String notes) {
        return new ProfessorAttendance(null, professorId, courseId, courseGroupId, status, timestamp, location, date, classType, notes);
    }
}
