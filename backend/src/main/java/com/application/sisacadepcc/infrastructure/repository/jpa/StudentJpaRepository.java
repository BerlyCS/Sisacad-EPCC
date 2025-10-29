package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentJpaRepository extends JpaRepository<StudentEntity, String> {

    boolean existsByCorreoInstitucional(String correoInstitucional);

    List<StudentEntity> findByAnio(Integer anio);

    // Este método ya existe por defecto con findById ya que el ID es String (documentoIdentidad)
    // Optional<StudentEntity> findById(String documentoIdentidad);
}
