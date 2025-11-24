package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.Schedule;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private Long classroomId;
    private Long userId; // Email del usuario que reserva
    private String purpose;
    private Schedule schedule;
    private LocalDate reservationDate; // specific date for reservation (optional)
    private LocalDateTime createdAt;
    private String status; // PENDING, APPROVED, REJECTED

    public Reservation() {}

    public Reservation(Long classroomId, Long userId, String purpose, Schedule schedule, LocalDate reservationDate) {
        this.classroomId = classroomId;
        this.userId = userId;
        this.purpose = purpose;
        this.schedule = schedule;
        this.reservationDate = reservationDate;
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
    public LocalDate getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
