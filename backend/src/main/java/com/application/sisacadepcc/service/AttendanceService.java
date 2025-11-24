package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Attendance;
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

    public AttendanceService(AttendanceRepository repository, StudentAttendanceRepository studentAttendanceRepository) {
        this.repository = repository;
        this.studentAttendanceRepository = studentAttendanceRepository;
    }

    public List<Attendance> getAll() { return repository.findAll(); }

    public Attendance getById(Long id) { return repository.findById(id); }

    public List<Attendance> getByProfessor(Long professorId) { return repository.findByProfessorId(professorId); }

    public List<Attendance> getByCourseGroup(Long courseGroupId) {
        return repository.findByCourseGroupId(courseGroupId);
    }

    public List<Attendance> getByCourse(Long courseId) { return repository.findByCourseId(courseId); }

    public List<Attendance> getByProfessorAndDate(Long professorId, LocalDate date) {
        return repository.findByProfessorIdAndDate(professorId, date);
    }

    @Transactional
        public Attendance markAttendance(Long professorId, Long courseId, Long courseGroupId,
                                     AttendanceStatus status, GeoLocation location,
                                     LocalDateTime timestamp, LocalDate date,
                                     ClassType classType, String todo) {
        Attendance attendance = new Attendance(
            null, professorId, courseId, courseGroupId, status,
                timestamp != null ? timestamp : LocalDateTime.now(),
                location, date != null ? date : LocalDate.now(),
                classType, todo
        );
        return repository.save(attendance);
    }

    @Transactional
    public Attendance createSession(Attendance session, List<StudentAttendance> students) {
        Attendance savedSession = repository.save(session);
        
        List<StudentAttendance> studentsWithId = students.stream()
            .map(s -> new StudentAttendance(null, savedSession.getAttendanceId(), s.getStudentId(), s.getStatus()))
            .collect(Collectors.toList());
            
        studentAttendanceRepository.saveAll(studentsWithId);
        return savedSession;
    }

    public List<com.application.sisacadepcc.domain.model.dto.StudentAttendanceDTO> getStudentAttendance(String studentId, Long courseId) {
        return studentAttendanceRepository.findDTOByStudentIdAndCourseId(studentId, courseId);
    }

    @Transactional
    public Attendance save(Attendance attendance) { return repository.save(attendance); }

    @Transactional
    public void delete(Long id) { repository.deleteById(id); }
}
