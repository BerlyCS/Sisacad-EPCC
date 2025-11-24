package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Student;
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
    public ResponseEntity<List<CourseGroup>> getLabSections(@PathVariable Long theoryCourseId,
                                                            Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.STUDENT, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<CourseGroup> groups = courseService.getLabGroupsForCourse(theoryCourseId);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{labCourseId}/validate")
    public ResponseEntity<EnrollmentValidationResult> validateLabEnrollment(@PathVariable Long labCourseId,
                                                                            @RequestBody(required = false) LabEnrollmentRequest request,
                                                                            Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.STUDENT, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long studentId = resolveStudentId(request, authentication);
        if (studentId == null) {
            return ResponseEntity.badRequest()
                    .body(EnrollmentValidationResult.failure("INVALID_STUDENT", "No se pudo identificar al estudiante", labCourseId, null));
        }

        EnrollmentValidationResult result = studentCourseService.validateLabEnrollment(studentId, labCourseId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{labCourseId}/confirm")
    public ResponseEntity<EnrollmentValidationResult> confirmLabEnrollment(@PathVariable Long labCourseId,
                                                                           @RequestBody(required = false) LabEnrollmentRequest request,
                                                                           Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.STUDENT, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long studentId = resolveStudentId(request, authentication);
        if (studentId == null) {
            return ResponseEntity.badRequest()
                    .body(EnrollmentValidationResult.failure("INVALID_STUDENT", "No se pudo identificar al estudiante", labCourseId, null));
        }

        EnrollmentValidationResult result = studentCourseService.confirmLabEnrollment(studentId, labCourseId);
        return ResponseEntity.ok(result);
    }

    private Long resolveStudentId(LabEnrollmentRequest request, Authentication authentication) {
        if (request != null && request.studentId() != null) {
            return request.studentId();
        }

        if (request != null && request.studentCui() != null && !request.studentCui().isBlank()) {
            return studentService.getStudentByCui(request.studentCui().trim())
                    .map(Student::getUserId)
                    .orElse(null);
        }

        return authorizationService.getAuthenticatedStudent(authentication)
                .map(Student::getUserId)
                .orElse(null);
    }
}
