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

@Service
public class StudentAttendanceService {

    private final StudentAttendanceRepository repository;
    private final AttendanceRepository attendanceRepository;

    public StudentAttendanceService(StudentAttendanceRepository repository, AttendanceRepository attendanceRepository) {
        this.repository = repository;
        this.attendanceRepository = attendanceRepository;
    }

    public List<StudentAttendance> getAll() { return repository.findAll(); }

    public StudentAttendance getById(Long id) { return repository.findById(id); }

    public List<StudentAttendance> getByStudent(String studentId) { return repository.findByStudentId(studentId); }

    public List<StudentAttendance> getByCourseGroup(Long courseGroupId) { return repository.findByCourseGroupId(courseGroupId); }

    public List<StudentAttendance> getByCourse(Long courseId) { return repository.findByCourseId(courseId); }

    public List<StudentAttendance> getByStudentAndDate(String studentId, LocalDate date) {
        return repository.findByStudentIdAndDate(studentId, date);
    }

    @Transactional
    public StudentAttendance markAttendance(String studentId, Long courseId, Long groupId,
                                            AttendanceStatus status, GeoLocation location,
                                            LocalDateTime timestamp, LocalDate date) {
        // Create a session for this single attendance
        ProfessorAttendance session = ProfessorAttendance.newSession(
            0L,
            courseId,
            groupId,
            AttendanceStatus.PRESENT,
            location,
            timestamp != null ? timestamp : LocalDateTime.now(),
            date != null ? date : LocalDate.now(),
            ClassType.THEORY,
            "Legacy single attendance"
        );
        ProfessorAttendance savedSession = attendanceRepository.save(session);

        StudentAttendance attendance = StudentAttendance.forSession(
            savedSession.getAttendanceId(),
            studentId,
            status
        );
        return repository.save(attendance);
    }

    @Transactional
    public StudentAttendance save(StudentAttendance attendance) { return repository.save(attendance); }

    @Transactional
    public void delete(Long id) { repository.deleteById(id); }
}
