package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Professor;
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
        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (matchesRoleAttribute(authentication, UserRole.ADMIN)) {
            return true;
        }

        return mapEmail(authentication)
                .map(email -> administratorRepository.findAll().stream()
                        .anyMatch(admin -> email.equalsIgnoreCase(admin.getInstitutionalEmail())))
                .orElse(false);
    }

    public boolean isProfessor(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (matchesRoleAttribute(authentication, UserRole.PROFESSOR)) {
            return true;
        }

        return mapEmail(authentication)
                .map(email -> professorRepository.findByCorreo(email).isPresent())
                .orElse(false);
    }

    public boolean isSecretary(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (matchesRoleAttribute(authentication, UserRole.SECRETARY)) {
            return true;
        }

        return mapEmail(authentication)
                .map(email -> secretaryRepository.findAll().stream()
                        .anyMatch(secretary -> email.equalsIgnoreCase(secretary.getInstitutionalEmail())))
                .orElse(false);
    }

    public boolean isStudent(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (matchesRoleAttribute(authentication, UserRole.STUDENT)) {
            return true;
        }

        return mapEmail(authentication)
                .flatMap(studentRepository::findByCorreoInstitucional)
                .isPresent();
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
        if (matchesRoleAttribute(authentication, role)) {
            return true;
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
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                Optional<Student> student = studentRepository.findByCorreoInstitucional(email);
                if (student.isPresent()) {
                    return student;
                }
            }

            Student fallback = buildStudentFromAttributes(oauth2User);
            if (fallback != null) {
                return Optional.of(fallback);
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

    public Optional<Professor> getAuthenticatedProfessor(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        return mapEmail(authentication)
                .flatMap(professorRepository::findByCorreo)
                .or(() -> buildProfessorFromAttributes(authentication));
    }

    private Optional<Professor> buildProfessorFromAttributes(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            String rawId = oauth2User.getAttribute("professorId");
            if (email != null || rawId != null) {
                Professor professor = new Professor();
                professor.setCorreo(email);
                professor.setNombres(oauth2User.getAttribute("name"));
                if (rawId != null) {
                    try {
                        professor.setId(Long.parseLong(rawId));
                    } catch (NumberFormatException ignored) {
                        professor.setId(null);
                    }
                }
                return Optional.of(professor);
            }
        }
        return Optional.empty();
    }

    private Student buildStudentFromAttributes(OAuth2User oauth2User) {
        String documento = oauth2User.getAttribute("documentoIdentidad");
        String email = oauth2User.getAttribute("email");
        String cui = oauth2User.getAttribute("cui");
        if (documento == null && email == null) {
            return null;
        }
        Student student = new Student();
        student.setDocumentoIdentidad(documento);
        student.setCui(cui);
        student.setNombres(oauth2User.getAttribute("name"));
        student.setCorreoInstitucional(email);
        return student;
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private boolean matchesRoleAttribute(Authentication authentication, UserRole expectedRole) {
        return resolveRoleFromAttributes(authentication)
                .map(role -> role == expectedRole)
                .orElse(false);
    }

    private Optional<String> mapEmail(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                return Optional.of(email);
            }
        }
        return Optional.empty();
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