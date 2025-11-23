package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Course;
import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    List<Course> findAll();
    Optional<Course> findById(Long id);
    Optional<Course> findByCourseCode(Long courseCode);
    Course save(Course course);
    void deleteById(Long courseId);
}
