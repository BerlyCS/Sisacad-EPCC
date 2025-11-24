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
        Administrator administrator = new Administrator();
        administrator.setUserId(entity.getUserId());
        administrator.setPaternalSurname(entity.getPaternalSurname());
        administrator.setMaternalSurname(entity.getMaternalSurname());
        administrator.setFirstNames(entity.getFirstNames());
        administrator.setInstitutionalEmail(entity.getInstitutionalEmail());
        administrator.setUserType(entity.getUserType());
        return administrator;
    }

    private AdministratorEntity mapToEntity(Administrator administrator) {
        AdministratorEntity entity = new AdministratorEntity();
        entity.setUserId(administrator.getUserId());
        entity.setPaternalSurname(administrator.getPaternalSurname());
        entity.setMaternalSurname(administrator.getMaternalSurname());
        entity.setFirstNames(administrator.getFirstNames());
        entity.setInstitutionalEmail(administrator.getInstitutionalEmail());
        entity.setUserType(administrator.getUserType());
        return entity;
    }

    @Override
    public boolean existsByInstitutionalEmail(String email) {
        return jpaRepository.existsByInstitutionalEmail(email);
    }

}
