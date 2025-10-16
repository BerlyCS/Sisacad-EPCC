package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.service.ProfessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professors")
public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @GetMapping
    @RequiresAdministratorAccess
    public ResponseEntity<List<Professor>> getAllProfessors() {
        return ResponseEntity.ok(service.getAllProfessors());
    }

    @PostMapping
    @RequiresAdministratorAccess
    public ResponseEntity<Professor> createProfessor(@RequestBody Professor professor) {
        // Lógica para crear profesor
        return ResponseEntity.ok(professor);
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
}