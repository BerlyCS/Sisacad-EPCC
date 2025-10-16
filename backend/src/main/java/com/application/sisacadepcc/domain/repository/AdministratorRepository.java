package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Administrator;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface AdministratorRepository {
    List<Administrator> findAll();
    boolean existsByInstitutionalEmail(String email);
}
