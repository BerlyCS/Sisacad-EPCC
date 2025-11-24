package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_attendances")
public class StudentAttendanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceEntity attendance;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "group_id")
    private Long courseGroupId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "check_timestamp", nullable = false)
    private LocalDateTime checkTimestamp;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    public StudentAttendanceEntity() {}

    public StudentAttendanceEntity(AttendanceEntity attendance,
                                   String studentId,
                                   AttendanceStatus status,
                                   Long courseId,
                                   Long courseGroupId,
                                   LocalDate date,
                                   LocalDateTime checkTimestamp,
                                   Double latitude,
                                   Double longitude) {
        this.attendance = attendance;
        this.studentId = studentId;
        this.status = status;
        this.courseId = courseId;
        this.courseGroupId = courseGroupId;
        this.date = date;
        this.checkTimestamp = checkTimestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public AttendanceEntity getAttendance() { return attendance; }
    public void setAttendance(AttendanceEntity attendance) { this.attendance = attendance; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getCourseGroupId() { return courseGroupId; }
    public void setCourseGroupId(Long courseGroupId) { this.courseGroupId = courseGroupId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalDateTime getCheckTimestamp() { return checkTimestamp; }
    public void setCheckTimestamp(LocalDateTime checkTimestamp) { this.checkTimestamp = checkTimestamp; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}
