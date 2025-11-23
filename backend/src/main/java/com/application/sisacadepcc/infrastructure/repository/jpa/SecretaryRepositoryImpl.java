package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.domain.repository.SecretaryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class SecretaryRepositoryImpl implements SecretaryRepository {

    private final SecretaryJpaRepository jpaRepository;

    public SecretaryRepositoryImpl(SecretaryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Secretary> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Secretary mapToDomain(SecretaryEntity entity) {
        Secretary secretary = new Secretary();
        secretary.setUserId(entity.getUserId());
        secretary.setDocumentId(entity.getDocumentId());
        secretary.setPaternalSurname(entity.getPaternalSurname());
        secretary.setMaternalSurname(entity.getMaternalSurname());
        secretary.setFirstNames(entity.getFirstNames());
        secretary.setInstitutionalEmail(entity.getInstitutionalEmail());
        secretary.setUserType(entity.getUserType());
        return secretary;
    }

    private SecretaryEntity mapToEntity(Secretary secretary) {
        SecretaryEntity entity = new SecretaryEntity();
        entity.setUserId(secretary.getUserId());
        entity.setDocumentId(secretary.getDocumentId());
        entity.setPaternalSurname(secretary.getPaternalSurname());
        entity.setMaternalSurname(secretary.getMaternalSurname());
        entity.setFirstNames(secretary.getFirstNames());
        entity.setInstitutionalEmail(secretary.getInstitutionalEmail());
        entity.setUserType(secretary.getUserType());
        return entity;
    }

    @Override
    public boolean existsByInstitutionalEmail(String email) {
        return jpaRepository.existsByInstitutionalEmail(email);
    }

}
