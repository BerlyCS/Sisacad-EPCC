package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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

    @Override
    public boolean existsByCorreo(String email) {
        return email != null && jpaRepository.findByInstitutionalEmail(email).isPresent();
    }

    @Override
    public Optional<Professor> findByCorreo(String correo) {
        return jpaRepository.findByInstitutionalEmail(correo)
                .map(this::mapToDomain);
    }

    private Professor mapToDomain(ProfessorEntity entity) {
        Professor professor = new Professor();
        professor.setUserId(entity.getUserId());
        professor.setDocumentId(entity.getDocumentId());
        professor.setPaternalSurname(entity.getPaternalSurname());
        professor.setMaternalSurname(entity.getMaternalSurname());
        professor.setFirstNames(entity.getFirstNames());
        professor.setInstitutionalEmail(entity.getInstitutionalEmail());
        professor.setUserType(entity.getUserType());
        return professor;
    }

    private ProfessorEntity mapToEntity(Professor professor) {
        ProfessorEntity entity = new ProfessorEntity();
        entity.setUserId(professor.getUserId());
        entity.setDocumentId(professor.getDocumentId());
        entity.setPaternalSurname(professor.getPaternalSurname());
        entity.setMaternalSurname(professor.getMaternalSurname());
        entity.setFirstNames(professor.getFirstNames());
        entity.setInstitutionalEmail(professor.getInstitutionalEmail());
        entity.setUserType(professor.getUserType());
        return entity;
    }
}
