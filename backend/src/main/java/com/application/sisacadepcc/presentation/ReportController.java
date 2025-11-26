package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.dto.AttendanceStatsDTO;
import com.application.sisacadepcc.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
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
}
