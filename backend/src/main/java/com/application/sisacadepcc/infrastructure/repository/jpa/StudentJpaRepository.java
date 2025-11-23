package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {

    boolean existsByCorreoInstitucional(String correoInstitucional);

    List<StudentEntity> findByAnio(Integer anio);

    // Agrega este método
    Optional<StudentEntity> findByCorreoInstitucional(String correoInstitucional);

    Optional<StudentEntity> findByCui(String cui);

    List<StudentEntity> findByDocumentoIdentidadIn(List<String> documentoIdentidad);

    Optional<StudentEntity> findByDocumentId(String documentId);
}