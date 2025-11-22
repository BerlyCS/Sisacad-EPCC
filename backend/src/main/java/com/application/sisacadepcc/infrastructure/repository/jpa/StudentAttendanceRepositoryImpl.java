package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.StudentAttendance;
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
    public List<StudentAttendance> findByGroupId(Long groupId) {
        return jpaRepository.findByAttendance_GroupId(groupId).stream().map(this::toDomain).collect(Collectors.toList());
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
        return new StudentAttendance(
                e.getId(),
                e.getAttendance().getAttendanceId(),
                e.getStudentId(),
                e.getStatus()
        );
    }

    private StudentAttendanceEntity toEntity(StudentAttendance a) {
        AttendanceEntity attendance = attendanceJpaRepository.findById(a.getAttendanceId())
                .orElseThrow(() -> new RuntimeException("Attendance session not found: " + a.getAttendanceId()));

        Double latitude = null;
        Double longitude = null;
        if (attendance.getLocation() != null) {
            latitude = attendance.getLocation().latitude();
            longitude = attendance.getLocation().longitude();
        }

        return new StudentAttendanceEntity(
                attendance,
                a.getStudentId(),
                a.getStatus(),
                attendance.getCourseId(),
                attendance.getGroupId(),
                attendance.getDate(),
                attendance.getTimestamp(),
                latitude,
                longitude
        );
    }
}
