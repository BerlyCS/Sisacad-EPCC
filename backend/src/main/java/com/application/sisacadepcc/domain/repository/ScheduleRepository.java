package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.infrastructure.repository.jpa.ScheduleEntity;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    List<ScheduleEntity> findAll();
    Optional<ScheduleEntity> findById(Long id);
    List<ScheduleEntity> findByCourseGroupId(Long courseGroupId);
    List<ScheduleEntity> findByClassroomId(Long classroomId);
    ScheduleEntity save(ScheduleEntity schedule);
    void deleteById(Long id);
    void deleteByCourseGroupId(Long courseGroupId);
}
