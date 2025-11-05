package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.repository.StudentAttendanceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class StudentAttendanceRepositoryImpl implements StudentAttendanceRepository {

    private final StudentAttendanceJpaRepository jpaRepository;

    public StudentAttendanceRepositoryImpl(StudentAttendanceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
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
    public StudentAttendance save(StudentAttendance attendance) {
        StudentAttendanceEntity entity = toEntity(attendance);
        StudentAttendanceEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<StudentAttendance> findByStudentId(String studentId) {
        return jpaRepository.findByStudentId(studentId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StudentAttendance> findByGroupId(Long groupId) {
        return jpaRepository.findByGroupId(groupId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StudentAttendance> findByCourseId(Long courseId) {
        return jpaRepository.findByCourseId(courseId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StudentAttendance> findByStudentIdAndDate(String studentId, java.time.LocalDate date) {
        return jpaRepository.findByStudentIdAndDate(studentId, date).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private StudentAttendance toDomain(StudentAttendanceEntity e) {
        return new StudentAttendance(
                e.getAttendanceId(),
                e.getStudentId(),
                e.getCourseId(),
                e.getGroupId(),
                e.getStatus(),
                e.getTimestamp(),
                e.getLocation(),
                e.getDate()
        );
    }

    private StudentAttendanceEntity toEntity(StudentAttendance a) {
        return new StudentAttendanceEntity(
                a.getStudentId(),
                a.getCourseId(),
                a.getGroupId(),
                a.getStatus(),
                a.getTimestamp(),
                a.getLocation(),
                a.getDate()
        );
    }
}
