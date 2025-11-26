package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeJpaRepository extends JpaRepository<GradeEntity, Long> {
    Optional<GradeEntity> findByCourseIdAndStudentId(Long courseId, Long studentId);

    List<GradeEntity> findByStudentId(Long studentId);

    List<GradeEntity> findByCourseId(Long courseId);
}