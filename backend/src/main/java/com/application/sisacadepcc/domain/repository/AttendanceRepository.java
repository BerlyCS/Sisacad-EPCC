package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.ProfessorAttendance;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository {
    List<ProfessorAttendance> findAll();
    ProfessorAttendance findById(Long id);
    ProfessorAttendance save(ProfessorAttendance attendance);
    void deleteById(Long id);

    List<ProfessorAttendance> findByProfessorId(Long professorId);
    List<ProfessorAttendance> findByCourseGroupId(Long courseGroupId);
    List<ProfessorAttendance> findByCourseId(Long courseId);
    List<ProfessorAttendance> findByProfessorIdAndDate(Long professorId, LocalDate date);
    List<ProfessorAttendance> findByCourseIdAndDateBetween(Long courseId, LocalDate startDate, LocalDate endDate);
    List<ProfessorAttendance> findByProfessorIdAndDateBetween(Long professorId, LocalDate startDate, LocalDate endDate);
}
