package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeJpaRepository extends JpaRepository<GradeEntity, Long> {
    Optional<GradeEntity> findByCourseCodeAndStudentId(String courseCode, Long studentId);

    List<GradeEntity> findByStudentId(Long studentId);

    List<GradeEntity> findByCourseCode(String courseCode);
}