package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Attendance;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository {
    List<Attendance> findAll();
    Attendance findById(Long id);
    Attendance save(Attendance attendance);
    void deleteById(Long id);

    List<Attendance> findByProfessorId(Long professorId);
    List<Attendance> findByGroupId(Long groupId);
    List<Attendance> findByCourseId(Long courseId);
    List<Attendance> findByProfessorIdAndDate(Long professorId, LocalDate date);
}
