package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.CourseGroup;
import java.util.List;
import java.util.Optional;

public interface CourseGroupRepository {
    List<CourseGroup> findAll();
    Optional<CourseGroup> findById(Long id);
    List<CourseGroup> findByCourseId(Long courseId);
    CourseGroup save(CourseGroup courseGroup);
    void deleteById(Long groupId);
}