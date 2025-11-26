package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.config.security.RequiresStudentAccess;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.presentation.dto.CourseGroupSummaryResponse;
import com.application.sisacadepcc.presentation.dto.CourseRosterPageResponse;
import com.application.sisacadepcc.presentation.dto.GradeSubmissionRequest;
import com.application.sisacadepcc.presentation.dto.GradeSubmissionResponse;
import com.application.sisacadepcc.presentation.dto.GradingRubricResponse;
import com.application.sisacadepcc.presentation.dto.ProfessorCourseGradeStatsResponse;
import com.application.sisacadepcc.presentation.dto.StudentGradeResponse;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.GradeQueryService;
import com.application.sisacadepcc.service.GradeService;
import com.application.sisacadepcc.service.GradeStatisticsService;
import com.application.sisacadepcc.service.ProfessorGradingService;
import com.application.sisacadepcc.service.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;
    private final GradeQueryService gradeQueryService;
    private final GradeStatisticsService gradeStatisticsService;
    private final ProfessorGradingService professorGradingService;
    private final AuthorizationService authorizationService;

    public GradeController(GradeService gradeService,
                           GradeQueryService gradeQueryService,
                           GradeStatisticsService gradeStatisticsService,
                           ProfessorGradingService professorGradingService,
                           AuthorizationService authorizationService) {
        this.gradeService = gradeService;
        this.gradeQueryService = gradeQueryService;
        this.gradeStatisticsService = gradeStatisticsService;
        this.professorGradingService = professorGradingService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @RequiresAdministratorAccess
    public List<Grade> getAllGrades() {
        return gradeService.getAllGrades();
    }

    @GetMapping("/students/{studentId}")
    @RequiresStudentAccess
    public ResponseEntity<List<StudentGradeResponse>> getGradesForStudent(@PathVariable Long studentId,
                                                                          Authentication authentication) {
        if (!ownsStudentRecord(studentId, authentication)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(gradeQueryService.getGradesForStudent(studentId));
    }

    @GetMapping("/students/{studentId}/courses/{courseId}")
    @RequiresStudentAccess
    public ResponseEntity<StudentGradeResponse> getGradeForCourse(@PathVariable Long studentId,
                                                                  @PathVariable Long courseId,
                                                                  Authentication authentication) {
        if (!ownsStudentRecord(studentId, authentication)) {
            return ResponseEntity.status(403).build();
        }

        return gradeQueryService.getGradeForCourse(studentId, courseId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/courses/{courseId}/statistics")
    public ResponseEntity<ProfessorCourseGradeStatsResponse> getCourseStatistics(@PathVariable Long courseId,
                                                                                Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        return gradeStatisticsService.getStatisticsForCourse(courseId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/courses/{courseId}/groups")
    public ResponseEntity<List<CourseGroupSummaryResponse>> getCourseGroups(@PathVariable Long courseId,
                                                                            Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        boolean isAdmin = authorizationService.hasRole(authentication, UserRole.ADMIN);

        try {
            return ResponseEntity.ok(professorGradingService.getCourseGroups(
                    courseId,
                    authorizationService.getAuthenticatedProfessor(authentication).orElse(null),
                    isAdmin
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<CourseRosterPageResponse> getCourseRoster(@PathVariable Long courseId,
                                                                    @RequestParam(name = "groupIds", required = false) List<Long> groupIds,
                                                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                                                    @RequestParam(name = "size", defaultValue = "150") int size,
                                                                    Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        boolean isAdmin = authorizationService.hasRole(authentication, UserRole.ADMIN);

        try {
            CourseRosterPageResponse response = professorGradingService.getCourseRoster(
                    courseId,
                    groupIds,
                    page,
                    size,
                    authorizationService.getAuthenticatedProfessor(authentication).orElse(null),
                    isAdmin
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/courses/{courseId}/rubric")
    public ResponseEntity<GradingRubricResponse> getCourseRubric(@PathVariable Long courseId,
                                                                 Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        boolean isAdmin = authorizationService.hasRole(authentication, UserRole.ADMIN);

        try {
            return ResponseEntity.ok(
                    professorGradingService.getCourseRubric(
                            courseId,
                            authorizationService.getAuthenticatedProfessor(authentication).orElse(null),
                            isAdmin
                    )
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/courses/{courseId}/students/{studentId}/groups/{groupId}")
    public ResponseEntity<GradeSubmissionResponse> submitGrade(@PathVariable Long courseId,
                                                               @PathVariable("studentId") Long studentId,
                                                               @PathVariable Long groupId,
                                                               @RequestBody GradeSubmissionRequest request,
                                                               Authentication authentication) {
        if (!authorizationService.hasRole(authentication, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        try {
            GradeSubmissionResponse response = professorGradingService.submitGrade(
                    courseId,
                    groupId,
                    studentId,
                    request,
                    authorizationService.getAuthenticatedProfessor(authentication).orElse(null)
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private boolean ownsStudentRecord(Long studentId, Authentication authentication) {
        return authorizationService.getAuthenticatedStudent(authentication)
                .map(student -> student.getUserId() != null && student.getUserId().equals(studentId))
                .orElse(false);
    }
}
