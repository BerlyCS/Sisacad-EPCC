package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    @RequiresAdministratorAccess
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(service.getAllStudents());
    }

    @PostMapping
    @RequiresAdministratorAccess
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        // Lógica para crear estudiante
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{documentoIdentidad}")
    @RequiresAdministratorAccess
    public ResponseEntity<Student> updateStudent(@PathVariable String documentoIdentidad, @RequestBody Student student) {
        // Lógica para actualizar estudiante
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{documentoIdentidad}")
    @RequiresAdministratorAccess
    public ResponseEntity<Void> deleteStudent(@PathVariable String documentoIdentidad) {
        // Lógica para eliminar estudiante
        return ResponseEntity.noContent().build();
    }
}