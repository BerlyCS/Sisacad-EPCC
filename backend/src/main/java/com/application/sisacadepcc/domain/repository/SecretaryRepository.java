package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Secretary;
import java.util.List;

public interface SecretaryRepository {
    List<Secretary> findAll();
    boolean existsByInstitutionalEmail(String email);
}
