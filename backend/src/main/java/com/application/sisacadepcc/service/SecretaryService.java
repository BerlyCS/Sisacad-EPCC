package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.domain.repository.SecretaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecretaryService {

    private final SecretaryRepository repository;

    public SecretaryService(SecretaryRepository repository) {
        this.repository = repository;
    }

    public List<Secretary> getAllSecretaries() {
        return repository.findAll();
    }
}
