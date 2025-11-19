package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Professor;

import java.util.List;
import java.util.Optional;

public interface ProfessorRepository {
    List<Professor> findAll();
    boolean existsByCorreo(String email);
    Optional<Professor> findByCorreo(String correo);
}
