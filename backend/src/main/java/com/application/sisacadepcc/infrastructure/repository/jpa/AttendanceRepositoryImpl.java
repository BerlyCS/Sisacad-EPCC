package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.ProfessorAttendance;
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
    public List<ProfessorAttendance> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public ProfessorAttendance findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain).orElse(null);
    }

    @Override
    public ProfessorAttendance save(ProfessorAttendance attendance) {
        AttendanceEntity entity = toEntity(attendance);
        AttendanceEntity saved = jpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<ProfessorAttendance> findByProfessorId(Long professorId) {
        return jpaRepository.findByProfessorId(professorId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ProfessorAttendance> findByCourseGroupId(Long courseGroupId) {
        return jpaRepository.findByCourseGroupId(courseGroupId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfessorAttendance> findByCourseId(Long courseId) {
        return jpaRepository.findByCourseId(courseId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public java.util.List<ProfessorAttendance> findByProfessorIdAndDate(Long professorId, java.time.LocalDate date) {
        return jpaRepository.findByProfessorIdAndDate(professorId, date).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ProfessorAttendance> findByCourseIdAndDateBetween(Long courseId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return jpaRepository.findByCourseIdAndDateBetween(courseId, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfessorAttendance> findByProfessorIdAndDateBetween(Long professorId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return jpaRepository.findByProfessorIdAndDateBetween(professorId, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ProfessorAttendance toDomain(AttendanceEntity e) {
        return new ProfessorAttendance(
                e.getAttendanceId(),
                e.getProfessorId(),
                e.getCourseId(),
                e.getCourseGroupId(),
                e.getStatus(),
                e.getTimestamp(),
                e.getLocation(),
                e.getDate(),
                e.getClassType(),
                e.getTodo()
        );
    }

    private AttendanceEntity toEntity(ProfessorAttendance a) {
        return new AttendanceEntity(
                a.getProfessorId(),
                a.getCourseId(),
                a.getCourseGroupId(),
                a.getStatus(),
                a.getTimestamp(),
                a.getLocation(),
                a.getDate(),
                a.getClassType(),
                a.getNotes()
        );
    }
}
