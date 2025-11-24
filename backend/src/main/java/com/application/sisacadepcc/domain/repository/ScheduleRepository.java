package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Schedule;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    List<Schedule> findAll();
    Optional<Schedule> findById(Long id);
    List<Schedule> findByCourseGroupId(Long courseGroupId);
    List<Schedule> findByClassroomId(Long classroomId);
    Schedule save(Schedule schedule);
    void deleteById(Long id);
    void deleteByCourseGroupId(Long courseGroupId);
}
