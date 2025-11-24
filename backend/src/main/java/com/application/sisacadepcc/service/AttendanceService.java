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

    public AttendanceService(AttendanceRepository repository, StudentAttendanceRepository studentAttendanceRepository) {
        this.repository = repository;
        this.studentAttendanceRepository = studentAttendanceRepository;
    }

    public List<ProfessorAttendance> getAll() { return repository.findAll(); }

    public ProfessorAttendance getById(Long id) { return repository.findById(id); }

    public List<ProfessorAttendance> getByProfessor(Long professorId) { return repository.findByProfessorId(professorId); }

    public List<ProfessorAttendance> getByCourseGroup(Long courseGroupId) {
        return repository.findByCourseGroupId(courseGroupId);
    }

    public List<ProfessorAttendance> getByCourse(Long courseId) { return repository.findByCourseId(courseId); }

    public List<ProfessorAttendance> getByProfessorAndDate(Long professorId, LocalDate date) {
        return repository.findByProfessorIdAndDate(professorId, date);
    }

    @Transactional
        public ProfessorAttendance markAttendance(Long professorId, Long courseId, Long courseGroupId,
                                     AttendanceStatus status, GeoLocation location,
                                     LocalDateTime timestamp, LocalDate date,
                                     ClassType classType, String todo) {
        ProfessorAttendance attendance = ProfessorAttendance.newSession(
                professorId,
                courseId,
                courseGroupId,
                status,
                location,
                timestamp != null ? timestamp : LocalDateTime.now(),
                date != null ? date : LocalDate.now(),
                classType,
                todo
        );
        return repository.save(attendance);
    }

    @Transactional
    public ProfessorAttendance createSession(ProfessorAttendance session, List<StudentAttendance> students) {
        ProfessorAttendance savedSession = repository.save(session);
        
        List<StudentAttendance> studentsWithId = students.stream()
            .map(s -> StudentAttendance.forSession(savedSession.getAttendanceId(), s.getStudentId(), s.getStatus()))
            .collect(Collectors.toList());
            
        studentAttendanceRepository.saveAll(studentsWithId);
        return savedSession;
    }

    public List<com.application.sisacadepcc.domain.model.dto.StudentAttendanceDTO> getStudentAttendance(String studentId, Long courseId) {
        return studentAttendanceRepository.findDTOByStudentIdAndCourseId(studentId, courseId);
    }

    @Transactional
    public ProfessorAttendance save(ProfessorAttendance attendance) { return repository.save(attendance); }

    @Transactional
    public void delete(Long id) { repository.deleteById(id); }
}
