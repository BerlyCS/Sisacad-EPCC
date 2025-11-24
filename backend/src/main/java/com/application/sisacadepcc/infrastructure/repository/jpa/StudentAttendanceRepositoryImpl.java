package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;
import com.application.sisacadepcc.domain.repository.StudentAttendanceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class StudentAttendanceRepositoryImpl implements StudentAttendanceRepository {

    private final StudentAttendanceJpaRepository jpaRepository;
    private final AttendanceJpaRepository attendanceJpaRepository;

    public StudentAttendanceRepositoryImpl(StudentAttendanceJpaRepository jpaRepository, AttendanceJpaRepository attendanceJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.attendanceJpaRepository = attendanceJpaRepository;
    }

    @Override
    public List<StudentAttendance> findByAttendanceId(Long attendanceId) {
        return jpaRepository.findByAttendance_AttendanceId(attendanceId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentAttendance> findByStudentId(String studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public StudentAttendance save(StudentAttendance attendance) {
        StudentAttendanceEntity entity = toEntity(attendance);
        StudentAttendanceEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void saveAll(List<StudentAttendance> studentAttendances) {
        List<StudentAttendanceEntity> entities = studentAttendances.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        jpaRepository.saveAll(entities);
    }

    @Override
    public List<StudentAttendance> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public StudentAttendance findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<StudentAttendance> findByCourseGroupId(Long courseGroupId) {
        return jpaRepository.findByAttendance_CourseGroupId(courseGroupId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StudentAttendance> findByCourseId(Long courseId) {
        return jpaRepository.findByAttendance_CourseId(courseId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StudentAttendance> findByStudentIdAndDate(String studentId, java.time.LocalDate date) {
        return jpaRepository.findByStudentIdAndAttendance_Date(studentId, date).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StudentAttendance> findByStudentIdAndCourseId(String studentId, Long courseId) {
        return jpaRepository.findByStudentIdAndCourseId(studentId, courseId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<com.application.sisacadepcc.domain.model.dto.StudentAttendanceDTO> findDTOByStudentIdAndCourseId(String studentId, Long courseId) {
        return jpaRepository.findDTOByStudentIdAndCourseId(studentId, courseId);
    }

    private StudentAttendance toDomain(StudentAttendanceEntity e) {
        GeoLocation location = (e.getLatitude() != null && e.getLongitude() != null)
                ? new GeoLocation(e.getLatitude(), e.getLongitude())
                : null;

        AttendanceEntity session = e.getAttendance();

        return new StudentAttendance(
                e.getId(),
                session.getAttendanceId(),
                e.getStudentId(),
                e.getStatus(),
                e.getCheckTimestamp(),
                location,
                e.getDate(),
                e.getCourseId(),
                e.getCourseGroupId(),
                session.getClassType(),
                session.getTodo()
        );
    }

    private StudentAttendanceEntity toEntity(StudentAttendance attendance) {
        Long sessionId = attendance.getSessionId() != null ? attendance.getSessionId() : attendance.getAttendanceId();
        AttendanceEntity session = attendanceJpaRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance session not found: " + sessionId));

        Double latitude = null;
        Double longitude = null;
        if (attendance.getLocation() != null) {
            latitude = attendance.getLocation().latitude();
            longitude = attendance.getLocation().longitude();
        }

        return new StudentAttendanceEntity(
                session,
                attendance.getStudentId(),
                attendance.getStatus(),
                session.getCourseId(),
                session.getCourseGroupId(),
                attendance.getDate() != null ? attendance.getDate() : session.getDate(),
                attendance.getTimestamp() != null ? attendance.getTimestamp() : session.getTimestamp(),
                latitude != null ? latitude : session.getLocation() != null ? session.getLocation().latitude() : null,
                longitude != null ? longitude : session.getLocation() != null ? session.getLocation().longitude() : null
        );
    }
}
