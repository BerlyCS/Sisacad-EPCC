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
import com.application.sisacadepcc.presentation.dto.CourseRosterEntryResponse;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
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
                    isAdmin));
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
                    isAdmin);
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
                            isAdmin));
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
                    authorizationService.getAuthenticatedProfessor(authentication).orElse(null));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(GradeSubmissionResponse.error(ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/courses/{courseId}/groups/{groupId}/bulk")
    public ResponseEntity<List<GradeSubmissionResponse>> submitGradesBulk(@PathVariable Long courseId,
            @PathVariable Long groupId,
            @RequestBody List<com.application.sisacadepcc.presentation.dto.StudentGradeSubmissionRequest> requests,
            Authentication authentication) {
        if (!authorizationService.hasRole(authentication, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        try {
            List<GradeSubmissionResponse> responses = professorGradingService.saveGradesBulk(
                    courseId,
                    groupId,
                    requests,
                    authorizationService.getAuthenticatedProfessor(authentication).orElse(null));
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
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
                            authentication));
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

    @GetMapping("/courses/{courseId}/groups/{groupId}/report")
    public ResponseEntity<Resource> downloadGradeReport(@PathVariable Long courseId,
            @PathVariable Long groupId,
            Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.PROFESSOR)) {
            return ResponseEntity.status(403).build();
        }

        boolean isAdmin = authorizationService.hasRole(authentication, UserRole.ADMIN);

        try {
            // Obtener roster
            CourseRosterPageResponse rosterResponse = professorGradingService.getCourseRoster(
                    courseId,
                    List.of(groupId),
                    0,
                    1000, // Asumir máximo 1000 estudiantes
                    authorizationService.getAuthenticatedProfessor(authentication).orElse(null),
                    isAdmin);

            // Obtener info del curso y grupo
            List<CourseGroupSummaryResponse> groups = professorGradingService.getCourseGroups(
                    courseId,
                    authorizationService.getAuthenticatedProfessor(authentication).orElse(null),
                    isAdmin);
            CourseGroupSummaryResponse group = groups.stream()
                    .filter(g -> g.groupId().equals(groupId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));

            // Asumir semesterNumber de algún lado; por ahora hardcode o buscar en Course
            // Para simplicidad, asumir par/impar basado en algo; aquí hardcode A para
            // ejemplo
            String ciclo = "A"; // Placeholder, ajustar con semesterNumber

            // Generar Excel
            byte[] excelBytes = generateGradeReportExcel(rosterResponse.students(), group.courseName(), ciclo,
                    group.groupLetter());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_notas.xlsx\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(excelBytes.length)
                    .body(new ByteArrayResource(excelBytes));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private byte[] generateGradeReportExcel(List<CourseRosterEntryResponse> students, String courseName, String ciclo,
            String groupLetter) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Calificaciones");

        int rowNum = 0;

        // Fila 0: vacía
        sheet.createRow(rowNum++);

        // Fila 1: UNIVERSIDAD NACIONAL DE SAN AGUSTIN DE AREQUIPA
        Row row1 = sheet.createRow(rowNum++);
        row1.createCell(0).setCellValue("UNIVERSIDAD NACIONAL DE SAN AGUSTIN DE AREQUIPA");

        // Fila 2: ESCUELA PROFESIONAL : CIENCIA DE LA COMPUTACIÓN
        Row row2 = sheet.createRow(rowNum++);
        row2.createCell(0).setCellValue("ESCUELA PROFESIONAL : CIENCIA DE LA COMPUTACIÓN");

        // Fila 3: CALIFICACIONES DE ALUMNOS
        Row row3 = sheet.createRow(rowNum++);
        row3.createCell(0).setCellValue("CALIFICACIONES DE ALUMNOS");

        // Fila 4: ASIGNATURA : {courseName}
        Row row4 = sheet.createRow(rowNum++);
        row4.createCell(0).setCellValue("ASIGNATURA : " + courseName);

        // Fila 5: CICLO : {ciclo} - GRUPO : {groupLetter}
        Row row5 = sheet.createRow(rowNum++);
        row5.createCell(0).setCellValue("CICLO : " + ciclo + " - GRUPO : " + groupLetter);

        // Fila 6: FECHA : {fecha}
        Row row6 = sheet.createRow(rowNum++);
        row6.createCell(0).setCellValue("FECHA : " + java.time.LocalDate.now());

        // 2 filas vacías
        sheet.createRow(rowNum++);
        sheet.createRow(rowNum++);

        // Fila headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = { "Nro", "CUI", "Apellidos y Nombres", "Nro. Matricula", "nota continua1", "nota examen 1",
                "nota continua 2", "nota examen 2", "nota continua 3", "nota examen 3" };
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Datos
        int nro = 1;
        for (CourseRosterEntryResponse student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(nro++);
            row.createCell(1).setCellValue(student.studentCui());
            row.createCell(2).setCellValue(student.fullName());
            row.createCell(3).setCellValue(""); // Nro. Matricula
            // Notas continuas
            for (int i = 0; i < 3 && i < student.continuousGrades().size(); i++) {
                row.createCell(4 + i * 2).setCellValue(student.continuousGrades().get(i));
            }
            // Notas exámenes
            for (int i = 0; i < 3 && i < student.examGrades().size(); i++) {
                row.createCell(5 + i * 2).setCellValue(student.examGrades().get(i));
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Escribir a bytes
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    private boolean ownsStudentRecord(Long studentId, Authentication authentication) {
        return authorizationService.getAuthenticatedStudent(authentication)
                .map(student -> student.getUserId() != null && student.getUserId().equals(studentId))
                .orElse(false);
    }
}
