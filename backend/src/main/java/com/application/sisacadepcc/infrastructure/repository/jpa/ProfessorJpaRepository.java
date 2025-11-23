package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorJpaRepository extends JpaRepository<ProfessorEntity, Long> {
    boolean existsByInstitutionalEmail(String institutionalEmail);
    Optional<ProfessorEntity> findByInstitutionalEmail(String institutionalEmail);
}
