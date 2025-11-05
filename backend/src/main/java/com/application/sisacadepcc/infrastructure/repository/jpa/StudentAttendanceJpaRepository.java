package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudentAttendanceJpaRepository extends JpaRepository<StudentAttendanceEntity, Long> {
    List<StudentAttendanceEntity> findByStudentId(String studentId);
    List<StudentAttendanceEntity> findByGroupId(Long groupId);
    List<StudentAttendanceEntity> findByCourseId(Long courseId);
    List<StudentAttendanceEntity> findByStudentIdAndDate(String studentId, LocalDate date);
}
