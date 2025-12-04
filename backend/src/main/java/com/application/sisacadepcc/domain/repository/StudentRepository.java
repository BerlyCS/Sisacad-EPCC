package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    List<Student> findAll();
    boolean existsByCorreoInstitucional(String email);
    List<Student> findByEnrollmentYear(Integer anio); // Cambiado de año a anio

    // Agregar este método para buscar por correo institucional
    Optional<Student> findByCorreoInstitucional(String correoInstitucional);

    Optional<Student> findByCui(String cui);

    Optional<Student> findById(Long id);

    Student save(Student student);
}
