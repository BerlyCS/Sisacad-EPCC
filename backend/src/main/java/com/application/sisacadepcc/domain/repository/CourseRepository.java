package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Course;
import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    List<Course> findAll();
    Optional<Course> findById(Long id);
    List<Course> findByAnio(Integer anio); // Cambiado de año a anio
    Optional<Course> updateTeacherAssignments(Long courseId, List<Long> teacherIds);
    List<Course> findByLabPrerequisiteCourseId(Long theoryCourseId);
    Optional<Course> updateLabCapacity(Long courseId, Integer labCapacity);
    Course save(Course course);
    void deleteById(Long courseId);
}
