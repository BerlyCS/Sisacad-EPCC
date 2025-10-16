package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.repository.AdministratorRepository;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import com.application.sisacadepcc.domain.repository.SecretaryRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorizationService {

    private final AdministratorRepository administratorRepository;
    private final ProfessorRepository professorRepository;
    private final SecretaryRepository secretaryRepository;
    private final StudentRepository studentRepository;

    public AuthorizationService(
            AdministratorRepository administratorRepository,
            ProfessorRepository professorRepository,
            SecretaryRepository secretaryRepository,
            StudentRepository studentRepository) {
        this.administratorRepository = administratorRepository;
        this.professorRepository = professorRepository;
        this.secretaryRepository = secretaryRepository;
        this.studentRepository = studentRepository;
    }

    public boolean isAdministrator(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                // Verificar si el email existe en la tabla de administradores
                return administratorRepository.findAll().stream()
                        .anyMatch(admin -> email.equalsIgnoreCase(admin.getInstitutionalEmail()));
            }
        }
        return false;
    }

    public boolean isProfessor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return professorRepository.findAll().stream()
                        .anyMatch(prof -> email.equalsIgnoreCase(prof.getCorreo()));
            }
        }
        return false;
    }

    public boolean isSecretary(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return secretaryRepository.findAll().stream()
                        .anyMatch(secretary -> email.equalsIgnoreCase(secretary.getInstitutionalEmail()));
            }
        }
        return false;
    }

    public boolean isStudent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return studentRepository.findAll().stream()
                        .anyMatch(student -> email.equalsIgnoreCase(student.getCorreoInstitucional()));
            }
        }
        return false;
    }

    public String getUserRole(Authentication authentication) {
        if (isAdministrator(authentication)) {
            return "ADMIN";
        } else if (isProfessor(authentication)) {
            return "PROFESSOR";
        } else if (isSecretary(authentication)) {
            return "SECRETARY";
        } else if (isStudent(authentication)) {
            return "STUDENT";
        }
        return "GUEST";
    }

    public boolean hasAccessToAllEndpoints(Authentication authentication) {
        return isAdministrator(authentication);
    }
}