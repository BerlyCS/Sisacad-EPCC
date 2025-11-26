package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.ProfessorAttendance;
import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.ClassType;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;
import com.application.sisacadepcc.domain.repository.AttendanceRepository;
import com.application.sisacadepcc.domain.repository.StudentAttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final SyllabusService syllabusService;

    public AttendanceService(AttendanceRepository repository,
            StudentAttendanceRepository studentAttendanceRepository,
            SyllabusService syllabusService) {
        this.repository = repository;
        this.studentAttendanceRepository = studentAttendanceRepository;
        this.syllabusService = syllabusService;
    }

    public List<ProfessorAttendance> getAll() {
        return repository.findAll();
    }

    public ProfessorAttendance getById(Long id) {
        return repository.findById(id);
    }

    public List<ProfessorAttendance> getByProfessor(Long professorId) {
        return repository.findByProfessorId(professorId);
    }

    public List<ProfessorAttendance> getByCourseGroup(Long courseGroupId) {
        return repository.findByCourseGroupId(courseGroupId);
    }

    public List<ProfessorAttendance> getByCourse(Long courseId) {
        return repository.findByCourseId(courseId);
    }

    public List<ProfessorAttendance> getByProfessorAndDate(Long professorId, LocalDate date) {
        return repository.findByProfessorIdAndDate(professorId, date);
    }

    public List<ProfessorAttendance> getHistory(Long professorId, Long courseId, LocalDate startDate,
            LocalDate endDate) {
        if (courseId != null && startDate != null && endDate != null) {
            return repository.findByCourseIdAndDateBetween(courseId, startDate, endDate);
        }
        if (professorId != null && startDate != null && endDate != null) {
            return repository.findByProfessorIdAndDateBetween(professorId, startDate, endDate);
        }
        // Fallback or other combinations can be added here
        return List.of();
    }

    @Transactional
    public ProfessorAttendance markAttendance(Long professorId, Long courseId, Long courseGroupId,
            AttendanceStatus status, GeoLocation location,
            LocalDateTime timestamp, LocalDate date,
            ClassType classType, String todo,
            java.time.LocalTime scheduledStartTime) {
        validateAttendanceRules(courseId, date, timestamp, scheduledStartTime, todo);

        ProfessorAttendance attendance = ProfessorAttendance.newSession(
                professorId,
                courseId,
                courseGroupId,
                status,
                location,
                timestamp != null ? timestamp : LocalDateTime.now(),
                date != null ? date : LocalDate.now(),
                classType,
                todo);
        return repository.save(attendance);
    }

    @Transactional
    public ProfessorAttendance createSession(ProfessorAttendance session, List<StudentAttendance> students,
            java.time.LocalTime scheduledStartTime) {
        validateAttendanceRules(session.getCourseId(), session.getDate(), session.getTimestamp(), scheduledStartTime,
                session.getNotes());

        ProfessorAttendance savedSession = repository.save(session);

        List<StudentAttendance> studentsWithId = students.stream()
                .map(s -> StudentAttendance.forSession(savedSession.getAttendanceId(), s.getStudentId(), s.getStatus()))
                .collect(Collectors.toList());

        studentAttendanceRepository.saveAll(studentsWithId);
        return savedSession;
    }

    private void validateAttendanceRules(Long courseId, LocalDate date, LocalDateTime timestamp,
            java.time.LocalTime scheduledStartTime, String todo) {
        // 1. Deadline Validation (48 hours)
        if (date.isBefore(LocalDate.now().minusDays(2))) {
            throw new IllegalArgumentException("Cannot register attendance for dates older than 48 hours.");
        }

        // 2. Tolerance Validation (15 minutes)
        // Only if we are registering for TODAY (real-time)
        if (date.isEqual(LocalDate.now()) && scheduledStartTime != null) {
            LocalDateTime scheduledStartDateTime = LocalDateTime.of(date, scheduledStartTime);
            long minutesDiff = java.time.Duration.between(scheduledStartDateTime, timestamp).toMinutes();
            if (minutesDiff > 15) {
                throw new IllegalArgumentException(
                        "Attendance registration is outside the 15-minute tolerance window.");
            }
        }

        // 3. Syllabus Enforcement
        if (todo != null && !todo.isBlank()) {
            // Check if the topic is in the syllabus (fuzzy check or just presence)
            // For now, we just ensure the syllabus exists for the course
            syllabusService.getByCourseId(courseId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Syllabus not found for this course. Please upload a syllabus first."));

            // Advanced: Check if 'todo' matches any topic name?
            // Let's keep it simple: just ensure syllabus exists so we know what SHOULD be
            // taught.
        }
    }

    public List<com.application.sisacadepcc.domain.model.dto.StudentAttendanceDTO> getStudentAttendance(
            String studentId, Long courseId) {
        return studentAttendanceRepository.findDTOByStudentIdAndCourseId(studentId, courseId);
    }

    @Transactional
    public ProfessorAttendance save(ProfessorAttendance attendance) {
        return repository.save(attendance);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
