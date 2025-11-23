package com.application.sisacadepcc.service;

import com.application.sisacadepcc.infrastructure.repository.jpa.AdministratorEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.AdministratorJpaRepository;
import com.application.sisacadepcc.infrastructure.repository.jpa.ProfessorEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.ProfessorJpaRepository;
import com.application.sisacadepcc.infrastructure.repository.jpa.SecretaryEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.SecretaryJpaRepository;
import com.application.sisacadepcc.infrastructure.repository.jpa.StudentEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.StudentJpaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class DemoUserService {

    private static final Logger log = LoggerFactory.getLogger(DemoUserService.class);

    private final boolean demoLoginEnabled;
    private final StudentJpaRepository studentJpaRepository;
    private final ProfessorJpaRepository professorJpaRepository;
    private final AdministratorJpaRepository administratorJpaRepository;
    private final SecretaryJpaRepository secretaryJpaRepository;
    private final Map<String, DemoUserProfile> demoProfiles;

    public DemoUserService(@Value("${app.demo.login.enabled:true}") boolean demoLoginEnabled,
                           StudentJpaRepository studentJpaRepository,
                           ProfessorJpaRepository professorJpaRepository,
                           AdministratorJpaRepository administratorJpaRepository,
                           SecretaryJpaRepository secretaryJpaRepository) {
        this.demoLoginEnabled = demoLoginEnabled;
        this.studentJpaRepository = studentJpaRepository;
        this.professorJpaRepository = professorJpaRepository;
        this.administratorJpaRepository = administratorJpaRepository;
        this.secretaryJpaRepository = secretaryJpaRepository;
        this.demoProfiles = Map.of(
        "STUDENT", new DemoUserProfile(
            "STUDENT",
            "Estudiante Demo",
            "student@example.org",
            "https://i.pravatar.cc/150?img=12",
            "11111111",
            "20250001"
        ),
                "PROFESSOR", new DemoUserProfile(
                        "PROFESSOR",
                        "Profesor Demo",
                        "professor@example.org",
            "https://i.pravatar.cc/150?img=11",
            null,
            null
                ),
                "ADMIN", new DemoUserProfile(
                        "ADMIN",
                        "Administrador Demo",
                        "admin@example.org",
            "https://i.pravatar.cc/150?img=10",
            null,
            null
                ),
                "SECRETARY", new DemoUserProfile(
                        "SECRETARY",
                        "Secretaria Demo",
                        "secretary@example.org",
            "https://i.pravatar.cc/150?img=9",
            null,
            null
                )
        );
    }

    public boolean isDemoLoginEnabled() {
        return demoLoginEnabled;
    }

    public Optional<DemoUserProfile> findProfileByRole(String role) {
        if (role == null) {
            return Optional.empty();
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(demoProfiles.get(normalized));
    }

    public List<DemoUserProfile> getProfiles() {
        return demoProfiles.values().stream().toList();
    }

    @PostConstruct
    @Transactional
    public void ensureDemoUsersExist() {
        if (!demoLoginEnabled) {
            return;
        }

        ensureStudent();
        ensureProfessor();
        ensureAdministrator();
        ensureSecretary();
    }

    private void ensureStudent() {
        DemoUserProfile profile = demoProfiles.get("STUDENT");
        if (profile == null) {
            return;
        }
        if (studentJpaRepository.existsByInstitutionalEmail(profile.email())) {
            return;
        }
        StudentEntity entity = new StudentEntity();
        entity.setDocumentId(profile.documentoIdentidad() != null ? profile.documentoIdentidad() : "11111111");
        entity.setCui(profile.cui() != null ? profile.cui() : "20250001");
        entity.setPaternalSurname("Demo");
        entity.setMaternalSurname("Student");
        entity.setFirstNames("Estudiante");
        entity.setInstitutionalEmail(profile.email());
        entity.setEnrollmentYear(2025);
        studentJpaRepository.save(entity);
    }

    private void ensureProfessor() {
        DemoUserProfile profile = demoProfiles.get("PROFESSOR");
        if (profile == null) {
            return;
        }
        if (professorJpaRepository.existsByInstitutionalEmail(profile.email())) {
            return;
        }

        ProfessorEntity entity = new ProfessorEntity();
        entity.setDocumentId("22222222");
        entity.setPaternalSurname("Demo");
        entity.setMaternalSurname("Professor");
        entity.setFirstNames("Profesor");
        entity.setInstitutionalEmail(profile.email());

        try {
            professorJpaRepository.save(entity);
        } catch (Exception ex) {
            log.warn("No se pudo registrar profesor demo: {}", ex.getMessage());
        }
    }

    private void ensureAdministrator() {
        DemoUserProfile profile = demoProfiles.get("ADMIN");
        if (profile == null) {
            return;
        }
        if (administratorJpaRepository.existsByInstitutionalEmail(profile.email())) {
            return;
        }
        AdministratorEntity entity = new AdministratorEntity();
        entity.setDocumentId("33333333");
        entity.setPaternalSurname("Demo");
        entity.setMaternalSurname("Admin");
        entity.setFirstNames("Administrador");
        entity.setInstitutionalEmail(profile.email());
        administratorJpaRepository.save(entity);
    }

    private void ensureSecretary() {
        DemoUserProfile profile = demoProfiles.get("SECRETARY");
        if (profile == null) {
            return;
        }
        if (secretaryJpaRepository.existsByInstitutionalEmail(profile.email())) {
            return;
        }
        SecretaryEntity entity = new SecretaryEntity();
        entity.setDocumentId("44444444");
        entity.setPaternalSurname("Demo");
        entity.setMaternalSurname("Secretary");
        entity.setFirstNames("Secretaria");
        entity.setInstitutionalEmail(profile.email());
        secretaryJpaRepository.save(entity);
    }

    public record DemoUserProfile(String role, String displayName, String email, String pictureUrl, String documentoIdentidad, String cui) {
    }
}
