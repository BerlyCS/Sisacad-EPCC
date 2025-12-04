package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.domain.repository.SecretaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class SecretaryService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final SecretaryRepository repository;

    public SecretaryService(SecretaryRepository repository) {
        this.repository = repository;
    }

    public List<Secretary> getAllSecretaries() {
        return repository.findAll();
    }

    @Transactional
    public Secretary createSecretary(Secretary secretary) {
        if (secretary == null) {
            throw new IllegalArgumentException("Los datos de la secretaria son obligatorios.");
        }

        String firstNames = sanitizeText(secretary.getFirstNames());
        String paternalSurname = sanitizeText(secretary.getPaternalSurname());
        String maternalSurname = sanitizeText(secretary.getMaternalSurname());
        String email = sanitizeEmail(secretary.getInstitutionalEmail());

        if (firstNames.isEmpty()) {
            throw new IllegalArgumentException("Ingresa los nombres de la secretaria.");
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

        if (repository.existsByInstitutionalEmail(email)) {
            throw new IllegalArgumentException("El correo institucional ya está registrado.");
        }

        Secretary toSave = new Secretary();
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
