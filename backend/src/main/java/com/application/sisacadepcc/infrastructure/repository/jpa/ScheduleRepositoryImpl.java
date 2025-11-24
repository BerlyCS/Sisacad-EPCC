package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.repository.ScheduleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ScheduleRepositoryImpl implements ScheduleRepository {

    private final ScheduleJpaRepository jpaRepository;

    public ScheduleRepositoryImpl(ScheduleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Schedule> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Schedule> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Schedule> findByCourseGroupId(Long courseGroupId) {
        if (courseGroupId == null) {
            return List.of();
        }
        return jpaRepository.findByCourseGroupId(courseGroupId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Schedule> findByClassroomId(Long classroomId) {
        if (classroomId == null) {
            return List.of();
        }
        return jpaRepository.findByClassroomId(classroomId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Schedule save(Schedule schedule) {
        ScheduleEntity entity = toEntity(schedule);
        ScheduleEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            jpaRepository.deleteById(id);
        }
    }

    @Override
    public void deleteByCourseGroupId(Long courseGroupId) {
        if (courseGroupId != null) {
            jpaRepository.deleteAssignmentsByCourseGroup(courseGroupId);
        }
    }

    private Schedule toDomain(ScheduleEntity entity) {
        Schedule schedule = new Schedule();
        schedule.setId(entity.getId());
        schedule.setDayOfWeek(entity.getDayOfWeek());
        schedule.setStartTime(entity.getStartTime());
        schedule.setEndTime(entity.getEndTime());
        schedule.setScheduleType(entity.getScheduleType());
        return schedule;
    }

    private ScheduleEntity toEntity(Schedule schedule) {
        ScheduleEntity entity = new ScheduleEntity();
        entity.setId(schedule.getId());
        entity.setDayOfWeek(schedule.getDayOfWeek());
        entity.setStartTime(schedule.getStartTime());
        entity.setEndTime(schedule.getEndTime());
        entity.setScheduleType(schedule.getScheduleType());
        return entity;
    }
}
