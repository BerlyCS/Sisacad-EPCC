package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    List<Student> findAll();
    boolean existsByCorreoInstitucional(String email);
    Optional<Student> findByDocumentoIdentidad(String documentoIdentidad);
    List<Student> findByAnio(Integer anio); // Cambiado de año a anio
}
