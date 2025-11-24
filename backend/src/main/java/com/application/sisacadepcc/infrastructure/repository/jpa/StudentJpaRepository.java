package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {

    boolean existsByInstitutionalEmail(String institutionalEmail);

    List<StudentEntity> findByEnrollmentYear(Integer anio);

    // Agrega este método
    Optional<StudentEntity> findByInstitutionalEmail(String institutionalEmail);

    Optional<StudentEntity> findByCui(String cui);

}