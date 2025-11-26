package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Can be student or professor ID (user_id from users table)

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String type; // INFO, WARNING, ERROR

    @Column(name = "is_read")
    private boolean read = false;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public NotificationEntity() {
    }

    public NotificationEntity(Long userId, String message, String type) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
