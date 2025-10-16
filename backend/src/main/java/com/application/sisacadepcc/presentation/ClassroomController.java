package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.service.ClassroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService service;

    public ClassroomController(ClassroomService service) {
        this.service = service;
    }

    @GetMapping
    @RequiresAdministratorAccess
    public ResponseEntity<List<Classroom>> getAllClassrooms() {
        return ResponseEntity.ok(service.getAllClassrooms());
    }

    @PostMapping
    @RequiresAdministratorAccess
    public ResponseEntity<Classroom> createClassroom(@RequestBody Classroom classroom) {
        // Lógica para crear aula
        return ResponseEntity.ok(classroom);
    }

    @PutMapping("/{id}")
    @RequiresAdministratorAccess
    public ResponseEntity<Classroom> updateClassroom(@PathVariable Long id, @RequestBody Classroom classroom) {
        // Lógica para actualizar aula
        return ResponseEntity.ok(classroom);
    }

    @DeleteMapping("/{id}")
    @RequiresAdministratorAccess
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        // Lógica para eliminar aula
        return ResponseEntity.noContent().build();
    }
}