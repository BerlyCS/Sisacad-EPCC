package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SecretaryJpaRepository extends JpaRepository<SecretaryEntity, Long> {
    boolean existsByInstitutionalEmail(String email);
    java.util.List<SecretaryEntity> findByInstitutionalEmail(String email);
}
