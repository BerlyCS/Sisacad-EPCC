package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseScheduleJpaRepository extends JpaRepository<CourseScheduleEntity, Long> {
    List<CourseScheduleEntity> findByCourseGroup_Id(Long courseGroupId);
    List<CourseScheduleEntity> findByCourseId(Long courseId);
    void deleteByCourseGroup_Id(Long courseGroupId);
}
