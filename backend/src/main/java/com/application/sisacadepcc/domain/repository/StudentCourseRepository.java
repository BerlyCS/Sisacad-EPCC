package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.StudentCourse;
import java.util.List;

public interface StudentCourseRepository {
    List<StudentCourse> findAll();
    List<StudentCourse> findByCourseId(Long courseId);
    List<StudentCourse> findByStudentDocumentoIdentidad(String studentDocumentoIdentidad);
    void save(StudentCourse studentCourse);
    void saveAll(List<StudentCourse> studentCourses);
    void deleteAll();
    boolean existsByStudentAndCourse(String studentDocumentoIdentidad, Long courseId);
}
