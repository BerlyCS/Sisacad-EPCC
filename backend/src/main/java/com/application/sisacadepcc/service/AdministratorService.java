package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Administrator;
import com.application.sisacadepcc.domain.repository.AdministratorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministratorService {
    private final AdministratorRepository repository;
    public AdministratorService(AdministratorRepository repository) {
        this.repository = repository;
    }

    public List<Administrator> getAllAdministators() {
        return repository.findAll();
    }
}
