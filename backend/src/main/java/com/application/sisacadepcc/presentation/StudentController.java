package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.service.StudentService;
import com.application.sisacadepcc.service.StudentCourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;
    private final StudentCourseService studentCourseService;

    public StudentController(StudentService service, StudentCourseService studentCourseService) {
        this.service = service;
        this.studentCourseService = studentCourseService;
    }

    @GetMapping
    @RequiresAdministratorAccess
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(service.getAllStudents());
    }

    @GetMapping("/{documentoIdentidad}/courses")
    @RequiresAdministratorAccess
    public ResponseEntity<List<Course>> getCoursesByStudent(@PathVariable String documentoIdentidad) {
        List<Course> courses = studentCourseService.getCoursesByStudent(documentoIdentidad);
        return ResponseEntity.ok(courses);
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
