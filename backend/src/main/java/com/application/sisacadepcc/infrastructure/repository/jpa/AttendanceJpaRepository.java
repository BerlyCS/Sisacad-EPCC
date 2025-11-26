package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceJpaRepository extends JpaRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByProfessorId(Long professorId);
    List<AttendanceEntity> findByCourseGroupId(Long courseGroupId);
    List<AttendanceEntity> findByCourseId(Long courseId);
    List<AttendanceEntity> findByProfessorIdAndDate(Long professorId, LocalDate date);
    List<AttendanceEntity> findByCourseIdAndDateBetween(Long courseId, LocalDate startDate, LocalDate endDate);
    List<AttendanceEntity> findByProfessorIdAndDateBetween(Long professorId, LocalDate startDate, LocalDate endDate);
}
