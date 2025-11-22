package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.presentation.dto.LabEnrollmentRequest;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.CourseService;
import com.application.sisacadepcc.service.StudentCourseService;
import com.application.sisacadepcc.service.StudentService;
import com.application.sisacadepcc.service.UserRole;
import com.application.sisacadepcc.service.dto.EnrollmentValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labs")
public class LabEnrollmentController {

    private final StudentCourseService studentCourseService;
    private final CourseService courseService;
    private final AuthorizationService authorizationService;
    private final StudentService studentService;

    public LabEnrollmentController(StudentCourseService studentCourseService,
                                   CourseService courseService,
                                   AuthorizationService authorizationService,
                                   StudentService studentService) {
        this.studentCourseService = studentCourseService;
        this.courseService = courseService;
        this.authorizationService = authorizationService;
        this.studentService = studentService;
    }

    @GetMapping("/course/{theoryCourseId}")
    public ResponseEntity<List<Course>> getLabSections(@PathVariable Long theoryCourseId,
                                                       Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.STUDENT, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Course> labs = courseService.getLabSectionsForTheoryCourse(theoryCourseId);
        return ResponseEntity.ok(labs);
    }

    @PostMapping("/{labCourseId}/validate")
    public ResponseEntity<EnrollmentValidationResult> validateLabEnrollment(@PathVariable Long labCourseId,
                                                                            @RequestBody(required = false) LabEnrollmentRequest request,
                                                                            Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.STUDENT, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String studentDocumento = resolveStudentDocumento(request, authentication);
        if (studentDocumento == null) {
            return ResponseEntity.badRequest()
                    .body(EnrollmentValidationResult.failure("INVALID_STUDENT", "No se pudo identificar al estudiante", labCourseId, null));
        }

        EnrollmentValidationResult result = studentCourseService.validateLabEnrollment(studentDocumento, labCourseId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{labCourseId}/confirm")
    public ResponseEntity<EnrollmentValidationResult> confirmLabEnrollment(@PathVariable Long labCourseId,
                                                                           @RequestBody(required = false) LabEnrollmentRequest request,
                                                                           Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.STUDENT, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String studentDocumento = resolveStudentDocumento(request, authentication);
        if (studentDocumento == null) {
            return ResponseEntity.badRequest()
                    .body(EnrollmentValidationResult.failure("INVALID_STUDENT", "No se pudo identificar al estudiante", labCourseId, null));
        }

        EnrollmentValidationResult result = studentCourseService.confirmLabEnrollment(studentDocumento, labCourseId);
        return ResponseEntity.ok(result);
    }

    private String resolveStudentDocumento(LabEnrollmentRequest request, Authentication authentication) {
        if (request != null && request.studentDocumentoIdentidad() != null && !request.studentDocumentoIdentidad().isBlank()) {
            return request.studentDocumentoIdentidad().trim();
        }

        if (request != null && request.studentCui() != null && !request.studentCui().isBlank()) {
            return studentService.getStudentByCui(request.studentCui().trim())
                    .map(student -> student.getDocumentoIdentidad() != null ? student.getDocumentoIdentidad().trim() : null)
                    .orElse(null);
        }

        return authorizationService.getAuthenticatedStudent(authentication)
                .map(student -> student.getDocumentoIdentidad() != null ? student.getDocumentoIdentidad().trim() : null)
                .orElse(null);
    }
}
