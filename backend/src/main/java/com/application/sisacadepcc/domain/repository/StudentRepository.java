package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Student;

import java.util.List;

public interface StudentRepository {
    List<Student> findAll();
    boolean existsByCorreoInstitucional(String email);
}
