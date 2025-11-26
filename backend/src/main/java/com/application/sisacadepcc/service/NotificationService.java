package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.repository.StudentAttendanceRepository;
import com.application.sisacadepcc.infrastructure.repository.jpa.NotificationEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.NotificationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationJpaRepository notificationRepository;
    private final StudentAttendanceRepository studentAttendanceRepository;

    public NotificationService(NotificationJpaRepository notificationRepository,
            StudentAttendanceRepository studentAttendanceRepository) {
        this.notificationRepository = notificationRepository;
        this.studentAttendanceRepository = studentAttendanceRepository;
    }

    public List<NotificationEntity> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<NotificationEntity> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void createNotification(Long userId, String message, String type) {
        NotificationEntity notification = new NotificationEntity(userId, message, type);
        notificationRepository.save(notification);
    }

    // Example logic to check alerts
    @Transactional
    public void checkAttendanceAlerts(Long courseId, String studentId, Long studentUserId) {
        List<StudentAttendance> records = studentAttendanceRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (records.isEmpty())
            return;

        long absent = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long total = records.size();
        double percentage = (double) absent / total;

        if (percentage > 0.30) {
            createNotification(studentUserId, "Alerta: Tienes más del 30% de inasistencias en el curso " + courseId,
                    "WARNING");
        }
    }
}
