package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.repository.AdministratorRepository;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import com.application.sisacadepcc.domain.repository.SecretaryRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

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
                return studentRepository.findByCorreoInstitucional(email).isPresent();
            }
        }
        return false;
    }

    public String getUserRole(Authentication authentication) {
        Optional<UserRole> sessionRole = resolveRoleFromAttributes(authentication);
        if (sessionRole.isPresent()) {
            return sessionRole.get().name();
        }

        if (hasRole(authentication, UserRole.ADMIN)) {
            return "ADMIN";
        } else if (hasRole(authentication, UserRole.PROFESSOR)) {
            return "PROFESSOR";
        } else if (hasRole(authentication, UserRole.SECRETARY)) {
            return "SECRETARY";
        } else if (hasRole(authentication, UserRole.STUDENT)) {
            return "STUDENT";
        }
        return "GUEST";
    }

    public boolean hasAccessToAllEndpoints(Authentication authentication) {
        return isAdministrator(authentication);
    }

    public boolean hasRole(Authentication authentication, UserRole role) {
        if (role == null) {
            return false;
        }
        return switch (role) {
            case ADMIN -> isAdministrator(authentication);
            case PROFESSOR -> isProfessor(authentication);
            case SECRETARY -> isSecretary(authentication);
            case STUDENT -> isStudent(authentication);
        };
    }

    public boolean hasAnyRole(Authentication authentication, UserRole... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        return Arrays.stream(roles)
                .anyMatch(role -> hasRole(authentication, role));
    }

    public Optional<Student> getAuthenticatedStudent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return studentRepository.findByCorreoInstitucional(email);
            }
        }
        return Optional.empty();
    }

    public Optional<String> getAuthenticatedStudentCui(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User oauth2User) {
                String cui = oauth2User.getAttribute("cui");
                if (cui != null && !cui.isBlank()) {
                    return Optional.of(cui);
                }
            }
        }

        return getAuthenticatedStudent(authentication).map(Student::getCui);
    }

    private Optional<UserRole> resolveRoleFromAttributes(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User && oauth2User.getAttributes() != null) {
            Object rawRoleObj = oauth2User.getAttributes().get("role");
            if (rawRoleObj instanceof String rawRole && !rawRole.isBlank()) {
                try {
                    return Optional.of(UserRole.valueOf(rawRole.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                    // Ignorar valores no mapeados para roles conocidos
                }
            }
        }

        return Optional.ofNullable(authentication.getAuthorities()).flatMap(authorities ->
                authorities.stream()
                        .map(granted -> granted.getAuthority())
                        .map(authority -> authority.replace("ROLE_", ""))
                        .map(authority -> {
                            try {
                                return UserRole.valueOf(authority.toUpperCase(Locale.ROOT));
                            } catch (IllegalArgumentException ex) {
                                return null;
                            }
                        })
                        .filter(role -> role != null)
                        .findFirst()
        );
    }
}