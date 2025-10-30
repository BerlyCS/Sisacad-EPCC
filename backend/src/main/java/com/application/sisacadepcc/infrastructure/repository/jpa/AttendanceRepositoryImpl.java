package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Attendance;
import com.application.sisacadepcc.domain.repository.AttendanceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AttendanceRepositoryImpl implements AttendanceRepository {

    private final AttendanceJpaRepository jpaRepository;

    public AttendanceRepositoryImpl(AttendanceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Attendance> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Attendance findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain).orElse(null);
    }

    @Override
    public Attendance save(Attendance attendance) {
        AttendanceEntity entity = toEntity(attendance);
        AttendanceEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Attendance> findByProfessorId(Long professorId) {
        return jpaRepository.findByProfessorId(professorId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Attendance> findByGroupId(Long groupId) {
        return jpaRepository.findByGroupId(groupId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Attendance> findByCourseId(Long courseId) {
        return jpaRepository.findByCourseId(courseId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public java.util.List<Attendance> findByProfessorIdAndDate(Long professorId, java.time.LocalDate date) {
        return jpaRepository.findByProfessorIdAndDate(professorId, date).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private Attendance toDomain(AttendanceEntity e) {
        return new Attendance(
                e.getAttendanceId(),
                e.getProfessorId(),
                e.getCourseId(),
                e.getGroupId(),
                e.getStatus(),
                e.getTimestamp(),
                e.getLocation(),
                e.getDate()
        );
    }

    private AttendanceEntity toEntity(Attendance a) {
        return new AttendanceEntity(
                a.getProfessorId(),
                a.getCourseId(),
                a.getGroupId(),
                a.getStatus(),
                a.getTimestamp(),
                a.getLocation(),
                a.getDate()
        );
    }
}
