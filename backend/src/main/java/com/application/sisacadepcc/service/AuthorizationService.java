package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Administrator;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.repository.AdministratorRepository;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import com.application.sisacadepcc.domain.repository.SecretaryRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthorizationService {

    private final AdministratorRepository administratorRepository;
    private final ProfessorRepository professorRepository;
    private final SecretaryRepository secretaryRepository;
    private final StudentRepository studentRepository;

    public AuthorizationService(AdministratorRepository administratorRepository,
                                ProfessorRepository professorRepository,
                                SecretaryRepository secretaryRepository,
                                StudentRepository studentRepository) {
        this.administratorRepository = administratorRepository;
        this.professorRepository = professorRepository;
        this.secretaryRepository = secretaryRepository;
        this.studentRepository = studentRepository;
    }

    public boolean isAdministrator(Authentication authentication) {
        return hasRole(authentication, UserRole.ADMIN);
    }

    public boolean isProfessor(Authentication authentication) {
        return hasRole(authentication, UserRole.PROFESSOR);
    }

    public boolean isSecretary(Authentication authentication) {
        return hasRole(authentication, UserRole.SECRETARY);
    }

    public boolean isStudent(Authentication authentication) {
        return hasRole(authentication, UserRole.STUDENT);
    }

    public String getUserRole(Authentication authentication) {
        return resolveDeclaredRole(authentication)
                .or(() -> deriveRoleFromRepositories(authentication))
                .map(Enum::name)
                .orElse("GUEST");
    }

    public boolean hasAccessToAllEndpoints(Authentication authentication) {
        return hasRole(authentication, UserRole.ADMIN);
    }

    public boolean hasRole(Authentication authentication, UserRole role) {
        if (role == null || !isAuthenticated(authentication)) {
            return false;
        }

        if (resolveDeclaredRole(authentication).filter(role::equals).isPresent()) {
            return true;
        }

        return extractEmail(authentication)
                .map(email -> repositoryHasRole(role, email))
                .orElse(false);
    }

    public boolean hasAnyRole(Authentication authentication, UserRole... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(role -> hasRole(authentication, role));
    }

    public Optional<Student> getAuthenticatedStudent(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        Optional<Student> persisted = extractEmail(authentication)
                .flatMap(studentRepository::findByCorreoInstitucional);
        if (persisted.isPresent()) {
            return persisted;
        }

        return asOauthUser(authentication).flatMap(this::buildStudentFromAttributes);
    }

    public Optional<String> getAuthenticatedStudentCui(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        Optional<String> cuiFromAttributes = asOauthUser(authentication)
                .map(user -> user.<String>getAttribute("cui"))
                .filter(value -> value != null && !value.isBlank());
        if (cuiFromAttributes.isPresent()) {
            return cuiFromAttributes;
        }

        return getAuthenticatedStudent(authentication).map(Student::getCui);
    }

    public Optional<Professor> getAuthenticatedProfessor(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        Optional<Professor> persisted = extractEmail(authentication)
                .flatMap(professorRepository::findByCorreo);
        if (persisted.isPresent()) {
            return persisted;
        }

        return asOauthUser(authentication).flatMap(this::buildProfessorFromAttributes);
    }

    public Optional<Secretary> getAuthenticatedSecretary(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        return extractEmail(authentication)
                .flatMap(secretaryRepository::findByInstitutionalEmail);
    }

    public Optional<Administrator> getAuthenticatedAdministrator(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        return extractEmail(authentication)
                .flatMap(administratorRepository::findByInstitutionalEmail);
    }

    private Optional<UserRole> deriveRoleFromRepositories(Authentication authentication) {
        return extractEmail(authentication).flatMap(email -> {
            if (repositoryHasRole(UserRole.ADMIN, email)) {
                return Optional.of(UserRole.ADMIN);
            }
            if (repositoryHasRole(UserRole.PROFESSOR, email)) {
                return Optional.of(UserRole.PROFESSOR);
            }
            if (repositoryHasRole(UserRole.SECRETARY, email)) {
                return Optional.of(UserRole.SECRETARY);
            }
            if (repositoryHasRole(UserRole.STUDENT, email)) {
                return Optional.of(UserRole.STUDENT);
            }
            return Optional.empty();
        });
    }

    private boolean repositoryHasRole(UserRole role, String email) {
        return switch (role) {
            case ADMIN -> administratorRepository.existsByInstitutionalEmail(email);
            case PROFESSOR -> professorRepository.existsByCorreo(email);
            case SECRETARY -> secretaryRepository.existsByInstitutionalEmail(email);
            case STUDENT -> studentRepository.findByCorreoInstitucional(email).isPresent();
        };
    }

    private Optional<UserRole> resolveDeclaredRole(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        Optional<UserRole> fromAttributes = asOauthUser(authentication)
                .flatMap(this::resolveRoleFromAttributes);
        if (fromAttributes.isPresent()) {
            return fromAttributes;
        }

        return resolveRoleFromAuthorities(authentication);
    }

    private Optional<UserRole> resolveRoleFromAttributes(OAuth2User oauth2User) {
        if (oauth2User.getAttributes() == null) {
            return Optional.empty();
        }

        Object direct = oauth2User.getAttributes().get("role");
        Optional<UserRole> parsed = parseRoleValue(direct);
        if (parsed.isPresent()) {
            return parsed;
        }

        Object multi = oauth2User.getAttributes().get("roles");
        return parseRoleValue(multi);
    }

    private Optional<UserRole> resolveRoleFromAuthorities(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null) {
            return Optional.empty();
        }

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority != null ? authority.replace("ROLE_", "") : null)
                .map(this::toUserRole)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<UserRole> parseRoleValue(Object raw) {
        if (raw == null) {
            return Optional.empty();
        }
        if (raw instanceof String single && !single.isBlank()) {
            return toUserRole(single);
        }
        if (raw instanceof Collection<?> collection) {
            return collection.stream()
                    .map(Object::toString)
                    .map(this::toUserRole)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        }
        return Optional.empty();
    }

    private Optional<UserRole> toUserRole(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UserRole.valueOf(candidate.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private Optional<String> extractEmail(Authentication authentication) {
        return asOauthUser(authentication)
                .map(user -> user.<String>getAttribute("email"))
                .filter(value -> value != null && !value.isBlank());
    }

    private Optional<OAuth2User> asOauthUser(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            return Optional.of(oauth2User);
        }
        return Optional.empty();
    }

    private Optional<Student> buildStudentFromAttributes(OAuth2User oauth2User) {
        String email = firstNonBlankAttribute(oauth2User, "email", "correo", "mail");
        if (email == null) {
            return Optional.empty();
        }

        Student student = new Student();
        student.setInstitutionalEmail(email);
        student.setCui(firstNonBlankAttribute(oauth2User, "cui"));
        student.setFirstNames(firstNonBlankAttribute(oauth2User, "firstNames", "given_name", "name"));
        student.setPaternalSurname(firstNonBlankAttribute(oauth2User, "paternalSurname", "apellidoPaterno"));
        student.setMaternalSurname(firstNonBlankAttribute(oauth2User, "maternalSurname", "apellidoMaterno"));
        return Optional.of(student);
    }

    private Optional<Professor> buildProfessorFromAttributes(OAuth2User oauth2User) {
        String email = firstNonBlankAttribute(oauth2User, "email", "correo", "mail");
        if (email == null) {
            return Optional.empty();
        }

        Professor professor = new Professor();
        professor.setInstitutionalEmail(email);
        professor.setFirstNames(firstNonBlankAttribute(oauth2User, "firstNames", "given_name", "name"));
        professor.setPaternalSurname(firstNonBlankAttribute(oauth2User, "paternalSurname", "apellidoPaterno"));
        professor.setMaternalSurname(firstNonBlankAttribute(oauth2User, "maternalSurname", "apellidoMaterno"));

        String rawId = firstNonBlankAttribute(oauth2User, "professorId", "id");
        if (rawId != null) {
            try {
                professor.setUserId(Long.parseLong(rawId));
            } catch (NumberFormatException ignored) {
                professor.setUserId(null);
            }
        }
        return Optional.of(professor);
    }

    private String firstNonBlankAttribute(OAuth2User oauth2User, String... keys) {
        if (oauth2User == null || oauth2User.getAttributes() == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            Object raw = oauth2User.getAttributes().get(key);
            if (raw instanceof String value && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}