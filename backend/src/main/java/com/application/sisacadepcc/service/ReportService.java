package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.ProfessorAttendance;
import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.dto.AttendanceStatsDTO;
import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.repository.AttendanceRepository;
import com.application.sisacadepcc.domain.repository.StudentAttendanceRepository;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

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
}
