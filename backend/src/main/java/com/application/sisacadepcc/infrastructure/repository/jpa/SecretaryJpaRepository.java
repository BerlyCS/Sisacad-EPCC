package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SecretaryJpaRepository extends JpaRepository<SecretaryEntity, String> {
    boolean existsByInstitutionalEmail(String email);
}
