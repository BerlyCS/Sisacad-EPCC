package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessorService {

    private final ProfessorRepository repository;

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    public List<Professor> getAllProfessors() {
        return repository.findAll();
    }
}
