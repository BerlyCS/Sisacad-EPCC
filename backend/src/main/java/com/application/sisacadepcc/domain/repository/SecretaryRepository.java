package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Secretary;
import java.util.List;
import java.util.Optional;

public interface SecretaryRepository {
    List<Secretary> findAll();
    boolean existsByInstitutionalEmail(String email);
    Optional<Secretary> findByInstitutionalEmail(String email);
}
