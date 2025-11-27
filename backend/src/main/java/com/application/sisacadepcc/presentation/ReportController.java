package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.dto.AttendanceStatsDTO;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.presentation.dto.CourseRosterEntryResponse;
import com.application.sisacadepcc.presentation.dto.CourseRosterPageResponse;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.ProfessorGradingService;
import com.application.sisacadepcc.service.ReportService;
import com.application.sisacadepcc.service.UserRole;
import com.application.sisacadepcc.service.dto.CourseGradeReportRow;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ProfessorGradingService professorGradingService;
    private final AuthorizationService authorizationService;
    private final CourseRepository courseRepository;
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    public ReportController(ReportService reportService,
            ProfessorGradingService professorGradingService,
            AuthorizationService authorizationService,
            CourseRepository courseRepository) {
        this.reportService = reportService;
        this.professorGradingService = professorGradingService;
        this.authorizationService = authorizationService;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/attendance/{courseId}/stats")
    public ResponseEntity<AttendanceStatsDTO> getStats(@PathVariable Long courseId) {
        return ResponseEntity.ok(reportService.getAttendanceStats(courseId));
    }

    @GetMapping("/attendance/{courseId}/export/excel")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long courseId) {
        try {
            byte[] excelContent = reportService.generateAttendanceReportExcel(courseId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelContent);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/courses/{courseId}/grades/export/excel")
    public ResponseEntity<byte[]> exportCourseGrades(@PathVariable Long courseId, Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.PROFESSOR)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isAdmin = authorizationService.hasRole(authentication, UserRole.ADMIN);
        Optional<com.application.sisacadepcc.domain.model.Professor> professor =
                authorizationService.getAuthenticatedProfessor(authentication);

        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

            CourseRosterPageResponse roster = professorGradingService.getCourseRoster(
                    courseId,
                    null,
                    0,
                    2000,
                    professor.orElse(null),
                    isAdmin);

                List<CourseRosterEntryResponse> entries = roster.students() != null ? roster.students() : List.of();
                List<CourseGradeReportRow> rows = entries.stream()
                    .map(this::toCourseGradeReportRow)
                    .toList();

            byte[] excel = reportService.generateCourseGradesExcel(
                    course.getName(),
                    resolveCourseCode(course),
                    LocalDateTime.now(),
                    rows);

            String fileName = String.format("grade_report_%s_%s.xlsx",
                    course.getCourseId() != null ? course.getCourseId() : "course",
                    FILE_TIMESTAMP_FORMATTER.format(LocalDateTime.now()));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private CourseGradeReportRow toCourseGradeReportRow(CourseRosterEntryResponse entry) {
        List<Integer> continuous = entry.continuousGrades() != null ? entry.continuousGrades() : List.of();
        List<Integer> exams = entry.examGrades() != null ? entry.examGrades() : List.of();
        return new CourseGradeReportRow(
                entry.fullName(),
                entry.studentCui(),
                entry.groupLetter(),
                continuous,
                exams,
                entry.finalGrade());
    }

    private String resolveCourseCode(Course course) {
        if (course == null) {
            return null;
        }
        if (course.getCourseCode() != null) {
            return course.getCourseCode().toString();
        }
        return course.getCourseId() != null ? course.getCourseId().toString() : null;
    }
}
