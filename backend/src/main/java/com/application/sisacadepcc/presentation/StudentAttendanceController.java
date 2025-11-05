package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;
import com.application.sisacadepcc.service.StudentAttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/student-attendances")
public class StudentAttendanceController {

    private final StudentAttendanceService service;

    public StudentAttendanceController(StudentAttendanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudentAttendance> list(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (studentId != null && date != null) {
            return service.getByStudentAndDate(studentId, date);
        }
        if (studentId != null) {
            return service.getByStudent(studentId);
        }
        if (groupId != null) {
            return service.getByGroup(groupId);
        }
        if (courseId != null) {
            return service.getByCourse(courseId);
        }
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentAttendance> get(@PathVariable Long id) {
        StudentAttendance attendance = service.getById(id);
        if (attendance == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(attendance);
    }

    @PostMapping
    public ResponseEntity<StudentAttendance> create(@RequestBody StudentAttendanceRequest request) {
        GeoLocation location = (request.latitude != null && request.longitude != null)
                ? new GeoLocation(request.latitude, request.longitude)
                : null;
        StudentAttendance created = service.markAttendance(
                request.studentId,
                request.courseId,
                request.groupId,
                request.status,
                location,
                request.timestamp,
                request.date
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public static class StudentAttendanceRequest {
        public String studentId;
        public Long courseId;
        public Long groupId;
        public AttendanceStatus status;
        public Double latitude;
        public Double longitude;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        public LocalDateTime timestamp;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate date;
    }
}
