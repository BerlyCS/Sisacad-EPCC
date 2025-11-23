package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleJpaRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByCourseGroup_Id(Long courseGroupId);
    List<ScheduleEntity> findByClassroom_ClassroomId(Long classroomId);
    void deleteByCourseGroup_Id(Long groupId);
}
