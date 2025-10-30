package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Attendance;
import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;
import com.application.sisacadepcc.domain.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;

    public AttendanceService(AttendanceRepository repository) {
        this.repository = repository;
    }

    public List<Attendance> getAll() { return repository.findAll(); }

    public Attendance getById(Long id) { return repository.findById(id); }

    public List<Attendance> getByProfessor(Long professorId) { return repository.findByProfessorId(professorId); }

    public List<Attendance> getByGroup(Long groupId) { return repository.findByGroupId(groupId); }

    public List<Attendance> getByCourse(Long courseId) { return repository.findByCourseId(courseId); }

    public List<Attendance> getByProfessorAndDate(Long professorId, LocalDate date) {
        return repository.findByProfessorIdAndDate(professorId, date);
    }

    @Transactional
    public Attendance markAttendance(Long professorId, Long courseId, Long groupId,
                                     AttendanceStatus status, GeoLocation location,
                                     LocalDateTime timestamp, LocalDate date) {
        Attendance attendance = new Attendance(
                null, professorId, courseId, groupId, status,
                timestamp != null ? timestamp : LocalDateTime.now(),
                location, date != null ? date : LocalDate.now()
        );
        return repository.save(attendance);
    }

    @Transactional
    public Attendance save(Attendance attendance) { return repository.save(attendance); }

    @Transactional
    public void delete(Long id) { repository.deleteById(id); }
}
