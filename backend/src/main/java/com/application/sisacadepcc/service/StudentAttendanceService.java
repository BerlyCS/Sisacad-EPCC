package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.StudentAttendance;
import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;
import com.application.sisacadepcc.domain.repository.StudentAttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentAttendanceService {

    private final StudentAttendanceRepository repository;

    public StudentAttendanceService(StudentAttendanceRepository repository) {
        this.repository = repository;
    }

    public List<StudentAttendance> getAll() { return repository.findAll(); }

    public StudentAttendance getById(Long id) { return repository.findById(id); }

    public List<StudentAttendance> getByStudent(String studentId) { return repository.findByStudentId(studentId); }

    public List<StudentAttendance> getByGroup(Long groupId) { return repository.findByGroupId(groupId); }

    public List<StudentAttendance> getByCourse(Long courseId) { return repository.findByCourseId(courseId); }

    public List<StudentAttendance> getByStudentAndDate(String studentId, LocalDate date) {
        return repository.findByStudentIdAndDate(studentId, date);
    }

    @Transactional
    public StudentAttendance markAttendance(String studentId, Long courseId, Long groupId,
                                            AttendanceStatus status, GeoLocation location,
                                            LocalDateTime timestamp, LocalDate date) {
        StudentAttendance attendance = new StudentAttendance(
                null, studentId, courseId, groupId, status,
                timestamp != null ? timestamp : LocalDateTime.now(),
                location, date != null ? date : LocalDate.now()
        );
        return repository.save(attendance);
    }

    @Transactional
    public StudentAttendance save(StudentAttendance attendance) { return repository.save(attendance); }

    @Transactional
    public void delete(Long id) { repository.deleteById(id); }
}
