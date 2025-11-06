package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.presentation.dto.StudentScheduleEntry;
import com.application.sisacadepcc.service.StudentService;
import com.application.sisacadepcc.service.StudentCourseService;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;
    private final StudentCourseService studentCourseService;
    private final StudentRepository studentRepository;

    public StudentController(StudentService service,
                             StudentCourseService studentCourseService,
                             StudentRepository studentRepository) {
        this.service = service;
        this.studentCourseService = studentCourseService;
        this.studentRepository = studentRepository;
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

    // NUEVO ENDPOINT para que estudiantes vean sus propios cursos - SIN anotación de administrador
    @GetMapping("/my-courses")
    public ResponseEntity<List<Course>> getMyCourses(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().build();
            }

            String email = principal.getAttribute("email");
            if (email == null) {
                return ResponseEntity.badRequest().build();
            }

            // Buscar el estudiante por email
            Optional<Student> student = studentRepository.findByCorreoInstitucional(email);
            if (student.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String studentDocumentoIdentidad = student.get().getDocumentoIdentidad();
            List<Course> courses = studentCourseService.getCoursesByStudent(studentDocumentoIdentidad);
            return ResponseEntity.ok(courses);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/my-schedule")
    public ResponseEntity<List<StudentScheduleEntry>> getMySchedule(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().build();
            }

            String email = principal.getAttribute("email");
            if (email == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Student> student = studentRepository.findByCorreoInstitucional(email);
            if (student.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String studentDocumentoIdentidad = student.get().getDocumentoIdentidad();
            List<StudentScheduleEntry> schedule = studentCourseService.getScheduleForStudent(studentDocumentoIdentidad);
            return ResponseEntity.ok(schedule);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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