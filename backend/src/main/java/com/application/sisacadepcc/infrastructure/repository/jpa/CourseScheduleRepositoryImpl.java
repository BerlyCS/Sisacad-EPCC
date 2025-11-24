package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.CourseSchedule;
import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.repository.CourseScheduleRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseScheduleRepositoryImpl implements CourseScheduleRepository {

    private final CourseScheduleJpaRepository jpaRepository;

    public CourseScheduleRepositoryImpl(CourseScheduleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<CourseSchedule> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<CourseSchedule> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<CourseSchedule> findByCourseGroupId(Long courseGroupId) {
        if (courseGroupId == null) {
            return List.of();
        }
        return jpaRepository.findByCourseGroup_Id(courseGroupId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<CourseSchedule> findByCourseId(Long courseId) {
        if (courseId == null) {
            return List.of();
        }
        return jpaRepository.findByCourseId(courseId).stream().map(this::toDomain).toList();
    }

    @Override
    public CourseSchedule save(CourseSchedule courseSchedule) {
        CourseScheduleEntity saved = jpaRepository.save(toEntity(courseSchedule));
        return toDomain(saved);
    }

    @Override
    public List<CourseSchedule> saveAll(List<CourseSchedule> courseSchedules) {
        if (courseSchedules == null || courseSchedules.isEmpty()) {
            return List.of();
        }
        List<CourseScheduleEntity> entities = courseSchedules.stream()
                .map(this::toEntity)
                .toList();
        return jpaRepository.saveAll(entities).stream().map(this::toDomain).toList();
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
            jpaRepository.deleteByCourseGroup_Id(courseGroupId);
        }
    }

    private CourseSchedule toDomain(CourseScheduleEntity entity) {
        CourseSchedule domain = new CourseSchedule();
        domain.setId(entity.getId());
        domain.setCourseId(entity.getCourseId());
        domain.setCourseGroupId(entity.getCourseGroup() != null ? entity.getCourseGroup().getId() : null);
        domain.setClassroomId(entity.getClassroom() != null ? entity.getClassroom().getClassroomId() : null);
        domain.setSequenceOrder(entity.getSequence());
        domain.setSchedule(toScheduleDomain(entity.getSchedule()));
        return domain;
    }

    private CourseScheduleEntity toEntity(CourseSchedule courseSchedule) {
        CourseScheduleEntity entity = new CourseScheduleEntity();
        entity.setId(courseSchedule.getId());
        entity.setCourseId(courseSchedule.getCourseId());
        if (courseSchedule.getCourseGroupId() != null) {
            CourseGroupEntity groupRef = new CourseGroupEntity();
            groupRef.setId(courseSchedule.getCourseGroupId());
            entity.setCourseGroup(groupRef);
        }
        if (courseSchedule.getClassroomId() != null) {
            entity.setClassroom(new ClassroomEntity(courseSchedule.getClassroomId(), null));
        }
        entity.setSequence(courseSchedule.getSequenceOrder());
        Schedule schedule = courseSchedule.getSchedule();
        if (schedule == null) {
            throw new IllegalArgumentException("CourseSchedule must include schedule information");
        }
        entity.setSchedule(toScheduleEntity(schedule));
        return entity;
    }

    private Schedule toScheduleDomain(ScheduleEntity entity) {
        if (entity == null) {
            return null;
        }
        Schedule schedule = new Schedule();
        schedule.setId(entity.getId());
        schedule.setDayOfWeek(entity.getDayOfWeek());
        schedule.setStartTime(entity.getStartTime());
        schedule.setEndTime(entity.getEndTime());
        schedule.setScheduleType(entity.getScheduleType());
        return schedule;
    }

    private ScheduleEntity toScheduleEntity(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        ScheduleEntity entity = new ScheduleEntity();
        entity.setId(schedule.getId());
        entity.setDayOfWeek(schedule.getDayOfWeek());
        entity.setStartTime(schedule.getStartTime());
        entity.setEndTime(schedule.getEndTime());
        entity.setScheduleType(schedule.getScheduleType());
        return entity;
    }
}
