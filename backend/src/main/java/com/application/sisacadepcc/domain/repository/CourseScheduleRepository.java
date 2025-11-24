package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.CourseSchedule;

import java.util.List;
import java.util.Optional;

public interface CourseScheduleRepository {
    List<CourseSchedule> findAll();
    Optional<CourseSchedule> findById(Long id);
    List<CourseSchedule> findByCourseGroupId(Long courseGroupId);
    List<CourseSchedule> findByCourseId(Long courseId);
    CourseSchedule save(CourseSchedule courseSchedule);
    List<CourseSchedule> saveAll(List<CourseSchedule> courseSchedules);
    void deleteById(Long id);
    void deleteByCourseGroupId(Long courseGroupId);
}
