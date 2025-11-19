package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.config.security.RequiresStudentAccess;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.presentation.dto.ProfessorCourseGradeStatsResponse;
import com.application.sisacadepcc.presentation.dto.StudentGradeResponse;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.GradeQueryService;
import com.application.sisacadepcc.service.GradeService;
import com.application.sisacadepcc.service.GradeStatisticsService;
import com.application.sisacadepcc.service.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;
    private final GradeQueryService gradeQueryService;
    private final GradeStatisticsService gradeStatisticsService;
    private final AuthorizationService authorizationService;

    public GradeController(GradeService gradeService,
                           GradeQueryService gradeQueryService,
                           GradeStatisticsService gradeStatisticsService,
                           AuthorizationService authorizationService) {
        this.gradeService = gradeService;
        this.gradeQueryService = gradeQueryService;
        this.gradeStatisticsService = gradeStatisticsService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @RequiresAdministratorAccess
    public List<Grade> getAllGrades() {
        return gradeService.getAllGrades();
    }

    @GetMapping("/students/{studentId}")
    @RequiresStudentAccess
    public ResponseEntity<List<StudentGradeResponse>> getGradesForStudent(@PathVariable String studentId,
                                                                          Authentication authentication) {
        if (!ownsStudentRecord(studentId, authentication)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(gradeQueryService.getGradesForStudent(studentId));
    }

    @GetMapping("/students/{studentId}/courses/{courseCode}")
    @RequiresStudentAccess
    public ResponseEntity<StudentGradeResponse> getGradeForCourse(@PathVariable String studentId,
                                                                  @PathVariable String courseCode,
                                                                  Authentication authentication) {
        if (!ownsStudentRecord(studentId, authentication)) {
            return ResponseEntity.status(403).build();
        }

        return gradeQueryService.getGradeForCourse(studentId, courseCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/courses/{courseCode}/statistics")
    public ResponseEntity<ProfessorCourseGradeStatsResponse> getCourseStatistics(@PathVariable String courseCode,
                                                                                Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        return gradeStatisticsService.getStatisticsForCourse(courseCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean ownsStudentRecord(String studentId, Authentication authentication) {
        return authorizationService.getAuthenticatedStudent(authentication)
                .map(student -> student.getDocumentoIdentidad().equalsIgnoreCase(studentId))
                .orElse(false);
    }
}
