package com.application.sisacadepcc.infrastructure.repository.jpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministratorJpaRepository extends JpaRepository<AdministratorEntity, String> {
    // you have already findAll()
}
