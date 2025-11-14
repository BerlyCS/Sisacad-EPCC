package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.presentation.dto.EnrollStudentRequest;
import com.application.sisacadepcc.presentation.dto.EnrollmentResponse;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.StudentCourseService;
import com.application.sisacadepcc.service.StudentService;
import com.application.sisacadepcc.service.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentCourseController {

    private final StudentCourseService studentCourseService;
    private final AuthorizationService authorizationService;
    private final StudentService studentService;

    public StudentCourseController(StudentCourseService studentCourseService,
                                  AuthorizationService authorizationService,
                                  StudentService studentService) {
        this.studentCourseService = studentCourseService;
        this.authorizationService = authorizationService;
        this.studentService = studentService;
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

    @PostMapping("/secretary/enrollments")
        public ResponseEntity<EnrollmentResponse> enrollStudentAsSecretary(
            @RequestBody EnrollStudentRequest request,
            Authentication authentication) {

        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request == null || request.courseId() == null || !request.hasStudentIdentifier()) {
            return ResponseEntity.badRequest()
                    .body(EnrollmentResponse.failure("Debe proporcionar el identificador del estudiante y el curso", null, request != null ? request.courseId() : null));
        }

        String studentDocumentId = resolveStudentDocumento(request);
        if (studentDocumentId == null) {
            return ResponseEntity.badRequest()
                    .body(EnrollmentResponse.failure("No se encontró al estudiante solicitado", null, request.courseId()));
        }

        try {
            studentCourseService.enrollStudentInCourse(studentDocumentId, request.courseId());
            return ResponseEntity.ok(EnrollmentResponse.success(studentDocumentId, request.courseId()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest()
                    .body(EnrollmentResponse.failure(ex.getMessage(), studentDocumentId, request.courseId()));
        }
    }

    private String resolveStudentDocumento(EnrollStudentRequest request) {
        if (request.studentDocumentoIdentidad() != null && !request.studentDocumentoIdentidad().isBlank()) {
            return request.studentDocumentoIdentidad().trim();
        }

        if (request.studentCui() == null || request.studentCui().isBlank()) {
            return null;
        }

        return studentService.getStudentByCui(request.studentCui().trim())
                .map(Student::getDocumentoIdentidad)
                .orElse(null);
    }
}
