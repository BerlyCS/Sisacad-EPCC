package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeJpaRepository extends JpaRepository<GradeEntity, Long> {
    Optional<GradeEntity> findByCourseCodeAndStudentDocumentoIdentidad(String courseCode, String studentDocumentoIdentidad);

    List<GradeEntity> findByStudentDocumentoIdentidad(String studentDocumentoIdentidad);

    List<GradeEntity> findByCourseCode(String courseCode);
}