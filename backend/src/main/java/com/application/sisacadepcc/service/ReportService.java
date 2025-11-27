package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.ProfessorAttendance;
import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.dto.AttendanceStatsDTO;
import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.repository.AttendanceRepository;
import com.application.sisacadepcc.domain.repository.StudentAttendanceRepository;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;
import com.application.sisacadepcc.service.dto.CourseGradeReportRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ReportService {

    private final AttendanceRepository attendanceRepository;
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final SyllabusRepository syllabusRepository;

    public ReportService(AttendanceRepository attendanceRepository,
            StudentAttendanceRepository studentAttendanceRepository,
            SyllabusRepository syllabusRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentAttendanceRepository = studentAttendanceRepository;
        this.syllabusRepository = syllabusRepository;
    }

    private static final DateTimeFormatter REPORT_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AttendanceStatsDTO getAttendanceStats(Long courseId) {
        List<ProfessorAttendance> sessions = attendanceRepository.findByCourseId(courseId);
        int totalSessions = sessions.size();

        // Note: This might be inefficient for large datasets.
        // Ideally, use a custom query to count statuses directly in the DB.
        List<StudentAttendance> studentAttendances = studentAttendanceRepository.findByCourseId(courseId);

        int present = (int) studentAttendances.stream().filter(s -> s.getStatus() == AttendanceStatus.PRESENT).count();
        int absent = (int) studentAttendances.stream().filter(s -> s.getStatus() == AttendanceStatus.ABSENT).count();
        int totalRecords = present + absent;

        double attendancePercentage = totalRecords == 0 ? 0 : (double) present / totalRecords * 100;

        // Syllabus Progress
        Syllabus syllabus = syllabusRepository.findByCourseId(courseId).orElse(null);
        double progress = 0;
        if (syllabus != null && !syllabus.getTopics().isEmpty()) {
            long completedTopics = sessions.stream()
                    .filter(s -> s.getNotes() != null && !s.getNotes().isBlank())
                    .count();
            int totalTopics = syllabus.getTopics().size();
            // Cap at 100%
            progress = Math.min(100.0, (double) completedTopics / totalTopics * 100);
        }

        return new AttendanceStatsDTO(attendancePercentage, progress, totalSessions, present, absent);
    }

    public byte[] generateAttendanceReportExcel(Long courseId) throws IOException {
        List<ProfessorAttendance> sessions = attendanceRepository.findByCourseId(courseId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance Report");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = { "Date", "Time", "Type", "Topic", "Status" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data
            int rowIdx = 1;
            for (ProfessorAttendance session : sessions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(session.getDate().toString());
                row.createCell(1).setCellValue(session.getTimestamp().toLocalTime().toString()); // Approximate time
                row.createCell(2).setCellValue(session.getClassType().toString());
                row.createCell(3).setCellValue(session.getNotes() != null ? session.getNotes() : "");
                row.createCell(4).setCellValue(session.getStatus().toString());
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] generateCourseGradesExcel(String courseName,
            String courseCode,
            LocalDateTime generatedAt,
            List<CourseGradeReportRow> rows) throws IOException {
        List<CourseGradeReportRow> safeRows = rows != null ? rows : List.of();
        LocalDateTime timestamp = Objects.requireNonNullElse(generatedAt, LocalDateTime.now());

        int maxContinuous = safeRows.stream()
                .mapToInt(row -> row.continuousGrades().size())
                .max()
                .orElse(0);
        int maxExam = safeRows.stream()
                .mapToInt(row -> row.examGrades().size())
                .max()
                .orElse(0);

        List<String> headers = new ArrayList<>();
        headers.add("#");
        headers.add("Estudiante");
        headers.add("CUI");
        headers.add("Grupo");
        for (int i = 0; i < maxContinuous; i++) {
            headers.add("Nota continua " + (i + 1));
        }
        for (int i = 0; i < maxExam; i++) {
            headers.add("Nota examen " + (i + 1));
        }
        headers.add("Nota final");

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte de notas");
            int rowIdx = 0;

            Row courseRow = sheet.createRow(rowIdx++);
            courseRow.createCell(0).setCellValue("Curso");
            courseRow.createCell(1).setCellValue(courseName != null ? courseName : "Curso");
            if (courseCode != null && !courseCode.isBlank()) {
                courseRow.createCell(2).setCellValue(courseCode);
            }

            Row generatedRow = sheet.createRow(rowIdx++);
            generatedRow.createCell(0).setCellValue("Generado");
            generatedRow.createCell(1).setCellValue(REPORT_TIMESTAMP_FORMATTER.format(timestamp));

            rowIdx++; // blank line for spacing

            Row headerRow = sheet.createRow(rowIdx++);
            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            int rowNumber = 1;
            for (CourseGradeReportRow entry : safeRows) {
                Row dataRow = sheet.createRow(rowIdx++);
                int colIdx = 0;
                dataRow.createCell(colIdx++).setCellValue(rowNumber++);
                dataRow.createCell(colIdx++).setCellValue(entry.studentName() != null ? entry.studentName() : "");
                dataRow.createCell(colIdx++).setCellValue(entry.studentCui() != null ? entry.studentCui() : "");
                dataRow.createCell(colIdx++).setCellValue(entry.groupLetter() != null ? entry.groupLetter() : "-");

                for (int i = 0; i < maxContinuous; i++) {
                    Double value = i < entry.continuousGrades().size()
                            ? safeNumber(entry.continuousGrades().get(i))
                            : null;
                    if (value != null) {
                        dataRow.createCell(colIdx++).setCellValue(value);
                    } else {
                        colIdx++;
                    }
                }

                for (int i = 0; i < maxExam; i++) {
                    Double value = i < entry.examGrades().size()
                            ? safeNumber(entry.examGrades().get(i))
                            : null;
                    if (value != null) {
                        dataRow.createCell(colIdx++).setCellValue(value);
                    } else {
                        colIdx++;
                    }
                }

                if (entry.finalGrade() != null) {
                    dataRow.createCell(colIdx).setCellValue(entry.finalGrade());
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private Double safeNumber(Number source) {
        if (source == null) {
            return null;
        }
        return source.doubleValue();
    }
}
