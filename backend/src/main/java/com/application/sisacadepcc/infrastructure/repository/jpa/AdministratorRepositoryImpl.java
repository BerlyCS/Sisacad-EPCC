package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Administrator;
import com.application.sisacadepcc.domain.repository.AdministratorRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AdministratorRepositoryImpl implements AdministratorRepository {

    private final AdministratorJpaRepository jpaRepository;

    public AdministratorRepositoryImpl(AdministratorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Administrator> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Administrator mapToDomain(AdministratorEntity entity) {
        return new Administrator(
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
