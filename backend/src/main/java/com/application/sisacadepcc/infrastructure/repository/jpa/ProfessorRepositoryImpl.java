package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProfessorRepositoryImpl implements ProfessorRepository {

    private final ProfessorJpaRepository jpaRepository;

    public ProfessorRepositoryImpl(ProfessorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Professor> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Professor mapToDomain(ProfessorEntity entity) {
        return new Professor(
                entity.getId(),
                entity.getApellidoPaterno(),
                entity.getApellidoMaterno(),
                entity.getNombres(),
                entity.getCorreo()
        );
    }

    @Override
    public boolean existsByCorreo(String email) {
        return jpaRepository.existsByCorreo(email);
    }

}
