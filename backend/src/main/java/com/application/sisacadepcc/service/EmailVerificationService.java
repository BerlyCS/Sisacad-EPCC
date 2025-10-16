package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import com.application.sisacadepcc.domain.repository.AdministratorRepository;
import com.application.sisacadepcc.domain.repository.SecretaryRepository;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {

    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final AdministratorRepository administratorRepository;
    private final SecretaryRepository secretaryRepository;

    public EmailVerificationService(
            StudentRepository studentRepository,
            ProfessorRepository professorRepository,
            AdministratorRepository administratorRepository,
            SecretaryRepository secretaryRepository) {
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.administratorRepository = administratorRepository;
        this.secretaryRepository = secretaryRepository;
    }

    public boolean verifyEmailExists(String email) {
        // Verificar en estudiantes
        boolean existsInStudents = studentRepository.findAll().stream()
                .anyMatch(student -> email.equalsIgnoreCase(student.getCorreoInstitucional()));

        // Verificar en profesores
        boolean existsInProfessors = professorRepository.findAll().stream()
                .anyMatch(professor -> email.equalsIgnoreCase(professor.getCorreo()));

        // Verificar en administradores
        boolean existsInAdministrators = administratorRepository.findAll().stream()
                .anyMatch(admin -> email.equalsIgnoreCase(admin.getInstitutionalEmail()));

        // Verificar en secretarias
        boolean existsInSecretaries = secretaryRepository.findAll().stream()
                .anyMatch(secretary -> email.equalsIgnoreCase(secretary.getInstitutionalEmail()));

        return existsInStudents || existsInProfessors || existsInAdministrators || existsInSecretaries;
    }
}