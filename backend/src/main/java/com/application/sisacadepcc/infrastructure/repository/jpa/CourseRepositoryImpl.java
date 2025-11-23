package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.valueobject.CourseSchedule;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseRepositoryImpl implements CourseRepository {

    private final CourseJpaRepository jpaRepository;

    public CourseRepositoryImpl(CourseJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Course> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Optional<Course> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public Course save(Course course) {
        CourseEntity entity = mapToEntity(course);
        CourseEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Course> findByCourseCode(Long courseCode) {
        return jpaRepository.findByCourseCode(courseCode)
                .map(this::mapToDomain);
    }

    @Override
    public void deleteById(Long courseId) {
        if (courseId != null) {
            jpaRepository.deleteById(courseId);
        }
    }

    private Course mapToDomain(CourseEntity entity) {
        Course course = new Course();
        course.setCourseId(entity.getCourseId());
        course.setCourseCode(entity.getCourseCode() != null ? entity.getCourseCode() : 0);
        course.setName(entity.getName());
        course.setCredits(entity.getCredits() != null ? entity.getCredits() : 0);
        course.setSyllabusId(entity.getSyllabusId());
        course.setLabHours(entity.getLabHours() != null ? entity.getLabHours() : 0);
        course.setPracticeHours(entity.getPracticeHours() != null ? entity.getPracticeHours() : 0);
        course.setTheoryHours(entity.getTheoryHours() != null ? entity.getTheoryHours() : 0);
        course.setSemesterNumber(entity.getSemesterNumber() != null ? entity.getSemesterNumber() : 0);
        course.setGroups(mapGroupsToDomain(entity.getGroups()));
        return course;
    }

    private List<CourseGroup> mapGroupsToDomain(List<CourseGroupEntity> groupEntities) {
        if (groupEntities == null || groupEntities.isEmpty()) {
            return new ArrayList<>();
        }
        List<CourseGroup> groups = new ArrayList<>();
        for (CourseGroupEntity groupEntity : groupEntities) {
            CourseGroup group = new CourseGroup();
            group.setId(groupEntity.getId());
            group.setLetter(groupEntity.getLetter());
            group.setType(groupEntity.getType());
            group.setMaxCapacity(groupEntity.getMaxCapacity());
            group.setAvailableCapacity(groupEntity.getAvailableCapacity());
            group.setTeacherId(groupEntity.getTeacherId());
            group.setScheduleSlots(mapScheduleSlotsToDomain(groupEntity.getSchedules()));
            groups.add(group);
        }
        return groups;
    }

    private List<CourseSchedule> mapScheduleSlotsToDomain(List<ScheduleEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        List<CourseSchedule> slots = new ArrayList<>();
        for (ScheduleEntity entity : entities) {
            CourseSchedule slot = new CourseSchedule(
                    entity.getClassroomName(),
                    entity.getDayOfWeek(),
                    entity.getStartTime(),
                    entity.getEndTime()
            );
            slots.add(slot);
        }
        return slots;
    }

    private List<ScheduleEntity> mapScheduleSlotsToEntity(List<CourseSchedule> schedules, CourseGroupEntity courseGroup) {
        if (schedules == null || schedules.isEmpty()) {
            return new ArrayList<>();
        }
        List<ScheduleEntity> entities = new ArrayList<>();
        for (CourseSchedule schedule : schedules) {
            ScheduleEntity entity = new ScheduleEntity();
            entity.setClassroomName(schedule.getClassroomName());
            entity.setDayOfWeek(schedule.getDayOfWeek());
            entity.setStartTime(schedule.getStartTime());
            entity.setEndTime(schedule.getEndTime());
            entity.setCourseGroup(courseGroup);
            entities.add(entity);
        }
        return entities;
    }

    private CourseEntity mapToEntity(Course course) {
        CourseEntity entity = new CourseEntity();
        entity.setCourseId(course.getCourseId());
        entity.setCourseCode(course.getCourseCode());
        entity.setName(course.getName());
        entity.setCredits(course.getCredits());
        entity.setSyllabusId(course.getSyllabusId());
        entity.setLabHours(course.getLabHours());
        entity.setPracticeHours(course.getPracticeHours());
        entity.setTheoryHours(course.getTheoryHours());
        entity.setSemesterNumber(course.getSemesterNumber());
        // Note: groups mapping may need to be handled separately for persistence
        return entity;
    }
}
