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
        return new Secretary(
                entity.getDni(),
                entity.getPaternalSurname(),
                entity.getMaternalSurname(),
                entity.getName(),
                entity.getInstitutionalEmail()
        );
    }

    @Override
    public boolean existsByInstitutionalEmail(String email) {
        return jpaRepository.existsByInstitutionalEmail(email);
    }

}
