package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ProfessorService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final ProfessorRepository repository;

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    public List<Professor> getAllProfessors() {
        return repository.findAll();
    }

    public Optional<Professor> getProfessorByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return repository.findByCorreo(email.trim().toLowerCase());
    }

    @Transactional
    public Professor createProfessor(Professor professor) {
        if (professor == null) {
            throw new IllegalArgumentException("Los datos del profesor son obligatorios.");
        }

        String firstNames = sanitizeText(professor.getFirstNames());
        String paternalSurname = sanitizeText(professor.getPaternalSurname());
        String maternalSurname = sanitizeText(professor.getMaternalSurname());
        String email = sanitizeEmail(professor.getInstitutionalEmail());

        if (firstNames.isEmpty()) {
            throw new IllegalArgumentException("Ingresa los nombres del profesor.");
        }
        if (paternalSurname.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el apellido paterno.");
        }
        if (maternalSurname.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el apellido materno.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Ingresa un correo institucional válido.");
        }

        if (repository.existsByCorreo(email)) {
            throw new IllegalArgumentException("El correo institucional ya está registrado.");
        }

        Professor toSave = new Professor();
        toSave.setFirstNames(firstNames);
        toSave.setPaternalSurname(paternalSurname);
        toSave.setMaternalSurname(maternalSurname);
        toSave.setInstitutionalEmail(email);

        return repository.save(toSave);
    }

    private String sanitizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String sanitizeEmail(String value) {
        return sanitizeText(value).toLowerCase();
    }
}
