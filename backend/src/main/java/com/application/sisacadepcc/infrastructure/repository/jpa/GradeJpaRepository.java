package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeJpaRepository extends JpaRepository<GradeEntity, Long> {
    Optional<GradeEntity> findByCourseIDAndStudentID(Long courseID, Long studentID);
}