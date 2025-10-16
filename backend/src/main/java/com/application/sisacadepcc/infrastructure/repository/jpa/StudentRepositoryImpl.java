package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

    private final StudentJpaRepository jpaRepository;

    public StudentRepositoryImpl(StudentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Student> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Student mapToDomain(StudentEntity entity) {
        return new Student(
                entity.getDocumentoIdentidad(),
                entity.getCui(),
                entity.getApellidoPaterno(),
                entity.getApellidoMaterno(),
                entity.getNombres(),
                entity.getCorreoInstitucional()
        );
    }

    @Override
    public boolean existsByCorreoInstitucional(String email) {
        return jpaRepository.existsByCorreoInstitucional(email);
    }

}
