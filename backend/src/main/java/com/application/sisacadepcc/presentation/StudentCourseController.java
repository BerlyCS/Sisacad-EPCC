package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.service.StudentCourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentCourseController {

    private final StudentCourseService studentCourseService;

    public StudentCourseController(StudentCourseService studentCourseService) {
        this.studentCourseService = studentCourseService;
    }

    @GetMapping("/courses/{courseId}/students")
    @RequiresAdministratorAccess
    public ResponseEntity<List<Student>> getStudentsByCourse(@PathVariable Long courseId) {
        List<Student> students = studentCourseService.getStudentsByCourse(courseId);
        return ResponseEntity.ok(students);
    }

    @PostMapping("/enrollments/student/{studentDocumentoIdentidad}/course/{courseId}")
    @RequiresAdministratorAccess
    public ResponseEntity<String> enrollStudentInCourse(
            @PathVariable String studentDocumentoIdentidad,
            @PathVariable Long courseId) {
        try {
            studentCourseService.enrollStudentInCourse(studentDocumentoIdentidad, courseId);
            return ResponseEntity.ok("Estudiante matriculado exitosamente");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
