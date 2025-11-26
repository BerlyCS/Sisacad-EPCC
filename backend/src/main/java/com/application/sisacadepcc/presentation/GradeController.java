package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.config.security.RequiresStudentAccess;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.presentation.dto.CourseGroupSummaryResponse;
import com.application.sisacadepcc.presentation.dto.CourseRosterPageResponse;
import com.application.sisacadepcc.presentation.dto.ExamSummaryResponse;
import com.application.sisacadepcc.presentation.dto.ExamSummaryUploadRequest;
import com.application.sisacadepcc.presentation.dto.GradeSubmissionRequest;
import com.application.sisacadepcc.presentation.dto.GradeSubmissionResponse;
import com.application.sisacadepcc.presentation.dto.GradingRubricResponse;
import com.application.sisacadepcc.presentation.dto.ProfessorCourseGradeStatsResponse;
import com.application.sisacadepcc.presentation.dto.StudentGradeResponse;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.CourseGroupExamsPdfService;
import com.application.sisacadepcc.service.GradeQueryService;
import com.application.sisacadepcc.service.GradeService;
import com.application.sisacadepcc.service.GradeStatisticsService;
import com.application.sisacadepcc.service.ProfessorGradingService;
import com.application.sisacadepcc.service.UserRole;
import com.application.sisacadepcc.service.exception.ExamSummaryAccessDeniedException;
import com.application.sisacadepcc.service.exception.ExamSummaryNotFoundException;
import com.application.sisacadepcc.service.exception.ExamSummaryValidationException;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;
    private final GradeQueryService gradeQueryService;
    private final GradeStatisticsService gradeStatisticsService;
    private final ProfessorGradingService professorGradingService;
    private final AuthorizationService authorizationService;
    private final CourseGroupExamsPdfService courseGroupExamsPdfService;

    public GradeController(GradeService gradeService,
                           GradeQueryService gradeQueryService,
                           GradeStatisticsService gradeStatisticsService,
                           ProfessorGradingService professorGradingService,
                           AuthorizationService authorizationService,
                           CourseGroupExamsPdfService courseGroupExamsPdfService) {
        this.gradeService = gradeService;
        this.gradeQueryService = gradeQueryService;
        this.gradeStatisticsService = gradeStatisticsService;
        this.professorGradingService = professorGradingService;
        this.authorizationService = authorizationService;
        this.courseGroupExamsPdfService = courseGroupExamsPdfService;
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

    @GetMapping("/groups/{groupId}/exam-summaries")
    public ResponseEntity<List<ExamSummaryResponse>> listExamSummaries(@PathVariable Long groupId,
                                                                       Authentication authentication) {
        try {
            List<ExamSummaryResponse> response = courseGroupExamsPdfService.listSummaries(groupId, authentication)
                    .stream()
                    .map(ExamSummaryResponse::from)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (ExamSummaryAccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ExamSummaryNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/groups/{groupId}/exam-summaries", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExamSummaryResponse> uploadExamSummary(@PathVariable Long groupId,
                                                                 @RequestPart("metadata") @Valid ExamSummaryUploadRequest request,
                                                                 @RequestPart("file") MultipartFile file,
                                                                 Authentication authentication) {
        try {
            ExamSummaryResponse response = ExamSummaryResponse.from(
                    courseGroupExamsPdfService.uploadSummary(
                            groupId,
                            request.examNumber(),
                            request.resolveSummaryType(),
                            file,
                            authentication
                    )
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ExamSummaryValidationException ex) {
            return ResponseEntity.badRequest().build();
        } catch (ExamSummaryAccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ExamSummaryNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/groups/{groupId}/exam-summaries/{summaryId}")
    public ResponseEntity<Void> deleteExamSummary(@PathVariable Long groupId,
                                                  @PathVariable Long summaryId,
                                                  Authentication authentication) {
        try {
            courseGroupExamsPdfService.deleteSummary(groupId, summaryId, authentication);
            return ResponseEntity.noContent().build();
        } catch (ExamSummaryValidationException ex) {
            return ResponseEntity.badRequest().build();
        } catch (ExamSummaryAccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ExamSummaryNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/groups/{groupId}/exam-summaries/{summaryId}/download")
    public ResponseEntity<Resource> downloadExamSummary(@PathVariable Long groupId,
                                                        @PathVariable Long summaryId,
                                                        Authentication authentication) {
        try {
            CourseGroupExamsPdfService.ExamPdfFile file = courseGroupExamsPdfService
                    .downloadSummary(groupId, summaryId, authentication);
            HttpHeaders headers = new HttpHeaders();
            String filename = file.summary().getContent() != null && file.summary().getContent().getName() != null
                    ? file.summary().getContent().getName()
                    : "exam-summary.pdf";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
                ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                    .headers(headers)
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_PDF));
            if (file.summary().getContent() != null && file.summary().getContent().getSizeBytes() != null) {
                builder.contentLength(file.summary().getContent().getSizeBytes());
            }
            return builder.body(file.resource());
        } catch (ExamSummaryAccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ExamSummaryNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean ownsStudentRecord(Long studentId, Authentication authentication) {
        return authorizationService.getAuthenticatedStudent(authentication)
                .map(student -> student.getUserId() != null && student.getUserId().equals(studentId))
                .orElse(false);
    }
}
