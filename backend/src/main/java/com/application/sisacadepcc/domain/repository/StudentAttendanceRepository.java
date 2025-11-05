package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.StudentAttendance;

import java.time.LocalDate;
import java.util.List;

public interface StudentAttendanceRepository {
    List<StudentAttendance> findAll();
    StudentAttendance findById(Long id);
    StudentAttendance save(StudentAttendance attendance);
    void deleteById(Long id);

    List<StudentAttendance> findByStudentId(String studentId);
    List<StudentAttendance> findByGroupId(Long groupId);
    List<StudentAttendance> findByCourseId(Long courseId);
    List<StudentAttendance> findByStudentIdAndDate(String studentId, LocalDate date);
}
