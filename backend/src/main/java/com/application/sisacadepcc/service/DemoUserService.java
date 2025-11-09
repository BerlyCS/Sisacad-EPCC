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
import jakarta.persistence.EntityManager;
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

    private static final long DEMO_PROFESSOR_ID = 22222222;

    private final boolean demoLoginEnabled;
    private final StudentJpaRepository studentJpaRepository;
    private final ProfessorJpaRepository professorJpaRepository;
    private final AdministratorJpaRepository administratorJpaRepository;
    private final SecretaryJpaRepository secretaryJpaRepository;
    private final EntityManager entityManager;
    private final Map<String, DemoUserProfile> demoProfiles;

    public DemoUserService(@Value("${app.demo.login.enabled:true}") boolean demoLoginEnabled,
                           StudentJpaRepository studentJpaRepository,
                           ProfessorJpaRepository professorJpaRepository,
                           AdministratorJpaRepository administratorJpaRepository,
                           SecretaryJpaRepository secretaryJpaRepository,
                           EntityManager entityManager) {
        this.demoLoginEnabled = demoLoginEnabled;
        this.studentJpaRepository = studentJpaRepository;
        this.professorJpaRepository = professorJpaRepository;
        this.administratorJpaRepository = administratorJpaRepository;
        this.secretaryJpaRepository = secretaryJpaRepository;
        this.entityManager = entityManager;
        this.demoProfiles = Map.of(
                "STUDENT", new DemoUserProfile(
                        "STUDENT",
                        "Estudiante Demo",
                        "student@example.org",
                        "https://i.pravatar.cc/150?img=12",
                        null
                ),
                "PROFESSOR", new DemoUserProfile(
                        "PROFESSOR",
                        "Profesor Demo",
                        "professor@example.org",
                        "https://i.pravatar.cc/150?img=11",
                        null
                ),
                "ADMIN", new DemoUserProfile(
                        "ADMIN",
                        "Administrador Demo",
                        "admin@example.org",
                        "https://i.pravatar.cc/150?img=10",
                        null
                ),
                "SECRETARY", new DemoUserProfile(
                        "SECRETARY",
                        "Secretaria Demo",
                        "secretary@example.org",
                        "https://i.pravatar.cc/150?img=9",
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
        if (studentJpaRepository.existsByCorreoInstitucional(profile.email())) {
            return;
        }
        StudentEntity entity = new StudentEntity();
        entity.setDocumentoIdentidad(profile.documentoIdentidad() != null ? profile.documentoIdentidad() : "11111111");
        entity.setCui("20250001");
        entity.setApellidoPaterno("Demo");
        entity.setApellidoMaterno("Student");
        entity.setNombres("Estudiante");
        entity.setCorreoInstitucional(profile.email());
        entity.setAnio(2025);
        studentJpaRepository.save(entity);
    }

    private void ensureProfessor() {
        DemoUserProfile profile = demoProfiles.get("PROFESSOR");
        if (profile == null) {
            return;
        }
        if (professorJpaRepository.existsByCorreo(profile.email())) {
            return;
        }
        syncIdentitySequence("docentes", "id");

        ProfessorEntity entity = professorJpaRepository.findById(DEMO_PROFESSOR_ID)
                .orElseGet(ProfessorEntity::new);

        entity.setId(DEMO_PROFESSOR_ID);
        entity.setApellidoPaterno("Demo");
        entity.setApellidoMaterno("Professor");
        entity.setNombres("Profesor");
        entity.setCorreo(profile.email());

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
        entity.setDni("33333333");
        entity.setPaternalSurname("Demo");
        entity.setMaternalSurname("Admin");
        entity.setName("Administrador");
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
        entity.setDni("44444444");
        entity.setPaternalSurname("Demo");
        entity.setMaternalSurname("Secretary");
        entity.setName("Secretaria");
        entity.setInstitutionalEmail(profile.email());
        secretaryJpaRepository.save(entity);
    }

    private void syncIdentitySequence(String tableName, String columnName) {
        try {
            Object sequenceNameResult = entityManager.createNativeQuery(
                            "SELECT pg_get_serial_sequence(:tableName, :columnName)")
                    .setParameter("tableName", tableName)
                    .setParameter("columnName", columnName)
                    .getSingleResult();

            if (sequenceNameResult == null) {
                return;
            }

            String sequenceName = sequenceNameResult.toString();
            String setvalSql = String.format(
                    "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s) + 1, false)",
                    sequenceName,
                    columnName,
                    tableName
            );
            entityManager.createNativeQuery(setvalSql).getSingleResult();
        } catch (Exception ex) {
            log.debug("No se pudo sincronizar la secuencia para {}.{}: {}", tableName, columnName, ex.getMessage());
        }
    }

    public record DemoUserProfile(String role, String displayName, String email, String pictureUrl, String documentoIdentidad) {
    }
}
