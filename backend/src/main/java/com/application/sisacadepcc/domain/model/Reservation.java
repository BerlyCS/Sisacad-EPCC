package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;
import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private String classroomName;
    private String reservedBy; // Email del usuario que reserva
    private String purpose;
    private OccupiedSchedule schedule;
    private LocalDateTime createdAt;
    private String status; // PENDING, APPROVED, REJECTED

    public Reservation() {}

    public Reservation(String classroomName, String reservedBy, String purpose, OccupiedSchedule schedule) {
        this.classroomName = classroomName;
        this.reservedBy = reservedBy;
        this.purpose = purpose;
        this.schedule = schedule;
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }
    public String getReservedBy() { return reservedBy; }
    public void setReservedBy(String reservedBy) { this.reservedBy = reservedBy; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public OccupiedSchedule getSchedule() { return schedule; }
    public void setSchedule(OccupiedSchedule schedule) { this.schedule = schedule; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
