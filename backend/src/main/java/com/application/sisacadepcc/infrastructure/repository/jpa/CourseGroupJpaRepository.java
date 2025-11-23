package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseGroupJpaRepository extends JpaRepository<CourseGroupEntity, Long> {

    List<CourseGroupEntity> findByCourseId(Long courseId);
    List<CourseGroupEntity> findByTeacherId(Long teacherId);
}