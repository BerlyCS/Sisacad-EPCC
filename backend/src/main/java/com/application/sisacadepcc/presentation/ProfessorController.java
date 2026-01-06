package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.presentation.dto.ProfessorScheduleEntry;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.CourseService;
import com.application.sisacadepcc.service.ProfessorService;
import com.application.sisacadepcc.service.UserRole;
import com.application.sisacadepcc.service.dto.UserImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/professors")
public class ProfessorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfessorController.class);

    private final ProfessorService service;
    private final AuthorizationService authorizationService;
    private final CourseService courseService;

    public ProfessorController(ProfessorService service,
                               AuthorizationService authorizationService,
                               CourseService courseService) {
        this.service = service;
        this.authorizationService = authorizationService;
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Professor>> getAllProfessors(Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(service.getAllProfessors());
    }

    @PostMapping
    @RequiresAdministratorAccess
    public ResponseEntity<?> createProfessor(@RequestBody Professor professor) {
        try {
            Professor created = service.createProfessor(professor);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            LOGGER.error("Error registrando profesor", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "No se pudo registrar el profesor"));
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserImportResult<Professor>> importProfessors(@RequestPart("file") MultipartFile file,
                                                                        Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        try {
            return ResponseEntity.ok(service.importProfessors(file));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new UserImportResult<>(List.of(), List.of(ex.getMessage()), 0, 0));
        }
    }

    @PutMapping("/{id}")
    @RequiresAdministratorAccess
    public ResponseEntity<Professor> updateProfessor(@PathVariable Long id, @RequestBody Professor professor) {
        // Lógica para actualizar profesor
        return ResponseEntity.ok(professor);
    }

    @DeleteMapping("/{id}")
    @RequiresAdministratorAccess
    public ResponseEntity<Void> deleteProfessor(@PathVariable Long id) {
        // Lógica para eliminar profesor
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Professor> getCurrentProfessor(Authentication authentication) {
        if (!authorizationService.hasRole(authentication, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        return authorizationService.getAuthenticatedProfessor(authentication)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @GetMapping("/me/courses")
    public ResponseEntity<List<Course>> getProfessorCourses(Authentication authentication) {
        if (!authorizationService.hasRole(authentication, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        return authorizationService.getAuthenticatedProfessor(authentication)
                .map(professor -> ResponseEntity.ok(courseService.getCoursesForProfessor(professor.getUserId())))
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @GetMapping("/me/schedule")
    public ResponseEntity<List<ProfessorScheduleEntry>> getProfessorSchedule(Authentication authentication) {
        if (!authorizationService.hasRole(authentication, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        return authorizationService.getAuthenticatedProfessor(authentication)
                .map(professor -> ResponseEntity.ok(courseService.getScheduleForProfessor(professor.getUserId())))
                .orElseGet(() -> ResponseEntity.status(404).build());
    }
}