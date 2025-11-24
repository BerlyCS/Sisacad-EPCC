package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.StudentAttendance;

import java.util.List;

public interface StudentAttendanceRepository {
    List<StudentAttendance> findByAttendanceId(Long attendanceId);
    List<StudentAttendance> findByStudentId(String studentId);
    StudentAttendance save(StudentAttendance studentAttendance);
    void saveAll(List<StudentAttendance> studentAttendances);
    
    // Legacy/Compatibility methods
    List<StudentAttendance> findAll();
    StudentAttendance findById(Long id);
    void deleteById(Long id);
    List<StudentAttendance> findByCourseGroupId(Long courseGroupId);
    List<StudentAttendance> findByCourseId(Long courseId);
    List<StudentAttendance> findByStudentIdAndDate(String studentId, java.time.LocalDate date);

    // Helper to find by student and course (via Attendance)
    List<StudentAttendance> findByStudentIdAndCourseId(String studentId, Long courseId);
    
    List<com.application.sisacadepcc.domain.model.dto.StudentAttendanceDTO> findDTOByStudentIdAndCourseId(String studentId, Long courseId);
}
