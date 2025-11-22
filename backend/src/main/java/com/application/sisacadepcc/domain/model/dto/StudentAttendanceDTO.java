package com.application.sisacadepcc.domain.model.dto;

import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.ClassType;
import java.time.LocalDate;

public class StudentAttendanceDTO {
    private Long id;
    private LocalDate date;
    private ClassType classType;
    private AttendanceStatus status;
    private String todo;

    public StudentAttendanceDTO(Long id, LocalDate date, ClassType classType, AttendanceStatus status, String todo) {
        this.id = id;
        this.date = date;
        this.classType = classType;
        this.status = status;
        this.todo = todo;
    }

    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public ClassType getClassType() { return classType; }
    public AttendanceStatus getStatus() { return status; }
    public String getTodo() { return todo; }
}
