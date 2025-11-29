package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.ProfessorAttendance;
import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.dto.AttendanceStatsDTO;
import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.ClassType;
import com.application.sisacadepcc.domain.model.valueobject.Topic;
import com.application.sisacadepcc.domain.repository.AttendanceRepository;
import com.application.sisacadepcc.domain.repository.StudentAttendanceRepository;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;
import com.application.sisacadepcc.service.dto.CourseGradeReportRow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentAttendanceRepository studentAttendanceRepository;

    @Mock
    private SyllabusRepository syllabusRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(attendanceRepository, studentAttendanceRepository, syllabusRepository);
    }

    @Test
    void getAttendanceStats_calculatesPercentagesAndProgress() {
        Long courseId = 7L;
        LocalDate baseDate = LocalDate.of(2024, 5, 10);

        List<ProfessorAttendance> sessions = List.of(
                ProfessorAttendance.newSession(10L, courseId, 20L, AttendanceStatus.PRESENT, null,
                        baseDate.atTime(8, 0), baseDate, ClassType.THEORY, "Intro"),
                ProfessorAttendance.newSession(10L, courseId, 21L, AttendanceStatus.PRESENT, null,
                        baseDate.plusDays(1).atTime(10, 0), baseDate.plusDays(1), ClassType.LABORATORY, "Lab 1")
        );
        when(attendanceRepository.findByCourseId(courseId)).thenReturn(sessions);

        List<StudentAttendance> attendanceRecords = List.of(
                StudentAttendance.forSession(100L, "20200001", AttendanceStatus.PRESENT),
                StudentAttendance.forSession(100L, "20200002", AttendanceStatus.PRESENT),
                StudentAttendance.forSession(101L, "20200003", AttendanceStatus.PRESENT),
                StudentAttendance.forSession(101L, "20200004", AttendanceStatus.ABSENT)
        );
        when(studentAttendanceRepository.findByCourseId(courseId)).thenReturn(attendanceRecords);

        Syllabus syllabus = new Syllabus(1L, courseId, null, List.of(
                new Topic("Intro", BigDecimal.ONE),
                new Topic("Lab", BigDecimal.ONE),
                new Topic("Project", BigDecimal.ONE),
                new Topic("Exam", BigDecimal.ONE)
        ));
        when(syllabusRepository.findByCourseId(courseId)).thenReturn(Optional.of(syllabus));

        AttendanceStatsDTO stats = reportService.getAttendanceStats(courseId);

        assertEquals(2, stats.getTotalSessions());
        assertEquals(3, stats.getPresentStudents());
        assertEquals(1, stats.getAbsentStudents());
        assertEquals(75.0, stats.getAttendancePercentage(), 0.001);
        assertEquals(50.0, stats.getSyllabusProgress(), 0.001);
    }

    @Test
    void generateCourseGradesExcel_buildsDynamicColumns() throws IOException {
        List<CourseGradeReportRow> rows = List.of(
                new CourseGradeReportRow("Ana Perez", "20201234", "A",
                        List.of(15, 16), List.of(18), 17.5),
                new CourseGradeReportRow("Luis Diaz", "20202345", "B",
                        List.of(14), List.of(17, 18), 16.0)
        );

        byte[] reportBytes = reportService.generateCourseGradesExcel(
                "Algoritmos", "CS101", LocalDateTime.of(2024, 1, 15, 9, 30), rows);

        assertNotNull(reportBytes);
        assertTrue(reportBytes.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportBytes))) {
            Sheet sheet = workbook.getSheet("Reporte de notas");
            assertNotNull(sheet);

            Row headerRow = sheet.getRow(3);
            assertEquals("Nota continua 2", headerRow.getCell(5).getStringCellValue());
            assertEquals("Nota examen 2", headerRow.getCell(7).getStringCellValue());
            assertEquals("Nota final", headerRow.getCell(8).getStringCellValue());

            Row firstDataRow = sheet.getRow(4);
            assertEquals(1, (int) firstDataRow.getCell(0).getNumericCellValue());
            assertEquals("Ana Perez", firstDataRow.getCell(1).getStringCellValue());
            assertEquals(17.5, firstDataRow.getCell(8).getNumericCellValue());

            Row secondDataRow = sheet.getRow(5);
            assertEquals(2, (int) secondDataRow.getCell(0).getNumericCellValue());
            assertEquals("Luis Diaz", secondDataRow.getCell(1).getStringCellValue());
            assertNull(secondDataRow.getCell(5));
            assertEquals(16.0, secondDataRow.getCell(8).getNumericCellValue());
        }
    }
}
