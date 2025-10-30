package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

    private final StudentJpaRepository studentJpaRepository;

    public StudentRepositoryImpl(StudentJpaRepository studentJpaRepository) {
        this.studentJpaRepository = studentJpaRepository;
    }

    @Override
    public List<Student> findAll() {
        return studentJpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Student> findByDocumentoIdentidad(String documentoIdentidad) {
        return studentJpaRepository.findById(documentoIdentidad)
                .map(this::mapToDomain);
    }

    @Override
    public List<Student> findByAnio(Integer anio) {
        return studentJpaRepository.findByAnio(anio)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCorreoInstitucional(String email) {
        return studentJpaRepository.existsByCorreoInstitucional(email);
    }

    @Override
    public Optional<Student> findByCorreoInstitucional(String correoInstitucional) {
        return studentJpaRepository.findByCorreoInstitucional(correoInstitucional)
                .map(this::mapToDomain);
    }

    private Student mapToDomain(StudentEntity entity) {
        return new Student(
                entity.getDocumentoIdentidad(),
                entity.getCui(),
                entity.getApellidoPaterno(),
                entity.getApellidoMaterno(),
                entity.getNombres(),
                entity.getCorreoInstitucional(),
                entity.getAnio()
        );
    }
}