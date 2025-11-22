package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;

public class StudentAttendance {

    private final Long id;
    private final Long attendanceId;
    private final String studentId;
    private final AttendanceStatus status;

    public StudentAttendance(Long id,
                             Long attendanceId,
                             String studentId,
                             AttendanceStatus status) {
        this.id = id;
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getAttendanceId() { return attendanceId; }
    public String getStudentId() { return studentId; }
    public AttendanceStatus getStatus() { return status; }
}
