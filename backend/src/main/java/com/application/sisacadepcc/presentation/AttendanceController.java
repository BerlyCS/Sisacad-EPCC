package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Attendance;
import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.ClassType;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;
import com.application.sisacadepcc.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Attendance> list(
            @RequestParam(required = false) Long professorId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (professorId != null && date != null) {
            return service.getByProfessorAndDate(professorId, date);
        }
        if (professorId != null) {
            return service.getByProfessor(professorId);
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
    public ResponseEntity<Attendance> get(@PathVariable Long id) {
        Attendance attendance = service.getById(id);
        if (attendance == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(attendance);
    }

    @PostMapping
    public ResponseEntity<Attendance> create(@RequestBody AttendanceRequest request) {
        GeoLocation location = (request.latitude != null && request.longitude != null)
                ? new GeoLocation(request.latitude, request.longitude)
                : null;
        Attendance created = service.markAttendance(
                request.professorId,
                request.courseId,
                request.groupId,
                request.status,
                location,
                request.timestamp,
                request.date,
                request.classType,
                request.todo
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/session")
    public ResponseEntity<Attendance> createSession(@RequestBody SessionRequest request) {
        GeoLocation location = (request.latitude != null && request.longitude != null)
                ? new GeoLocation(request.latitude, request.longitude)
                : null;
        
        Attendance session = new Attendance(
                null, request.professorId, request.courseId, request.groupId,
                AttendanceStatus.PRESENT,
                request.timestamp != null ? request.timestamp : LocalDateTime.now(),
                location,
                request.date != null ? request.date : LocalDate.now(),
                request.classType,
                request.todo
        );
        
        List<StudentAttendance> students = request.students.stream()
                .map(s -> new StudentAttendance(null, null, s.studentId, s.status))
                .collect(Collectors.toList());

        Attendance created = service.createSession(session, students);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public List<com.application.sisacadepcc.domain.model.dto.StudentAttendanceDTO> getStudentAttendance(@PathVariable String studentId, @PathVariable Long courseId) {
        return service.getStudentAttendance(studentId, courseId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public static class AttendanceRequest {
        public Long professorId;
        public Long courseId;
        public Long groupId;
        public AttendanceStatus status;
        public Double latitude;
        public Double longitude;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        public LocalDateTime timestamp;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate date;
        public ClassType classType;
        public String todo;
    }

    public static class SessionRequest {
        public Long professorId;
        public Long courseId;
        public Long groupId;
        public Double latitude;
        public Double longitude;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        public LocalDateTime timestamp;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate date;
        public ClassType classType;
        public String todo;
        public List<StudentAttendanceRequest> students;
    }

    public static class StudentAttendanceRequest {
        public String studentId;
        public AttendanceStatus status;
    }
}
