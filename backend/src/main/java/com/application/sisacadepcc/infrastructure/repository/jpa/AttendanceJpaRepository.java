package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceJpaRepository extends JpaRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByProfessorId(Long professorId);
    List<AttendanceEntity> findByGroupId(Long groupId);
    List<AttendanceEntity> findByCourseId(Long courseId);
    List<AttendanceEntity> findByProfessorIdAndDate(Long professorId, LocalDate date);
}
