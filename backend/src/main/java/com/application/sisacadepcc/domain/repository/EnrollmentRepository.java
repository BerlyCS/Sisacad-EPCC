package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.StudentCourse;
import java.util.List;

public interface EnrollmentRepository {
    List<StudentCourse> findAll();
    List<StudentCourse> findByCourseGroupId(Long courseGroupId);
    List<StudentCourse> findByStudentCui(String studentCui);
    void save(StudentCourse studentCourse);
    void saveAll(List<StudentCourse> studentCourses);
    void deleteAll();
    boolean existsByStudentAndCourseGroup(String studentCui, Long courseGroupId);
    long countByCourseGroupId(Long courseGroupId);
}
