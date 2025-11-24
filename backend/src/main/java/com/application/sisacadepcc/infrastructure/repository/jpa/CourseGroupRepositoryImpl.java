package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseSchedule;
import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.model.valueobject.ScheduleType;
import com.application.sisacadepcc.domain.repository.CourseGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class CourseGroupRepositoryImpl implements CourseGroupRepository {

    private final CourseGroupJpaRepository jpaRepository;

    public CourseGroupRepositoryImpl(CourseGroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<CourseGroup> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Optional<CourseGroup> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public List<CourseGroup> findByCourseId(Long courseId) {
        return jpaRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<CourseGroup> findByTeacherId(Long teacherId) {
        return jpaRepository.findByTeacherId(teacherId)
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public CourseGroup save(CourseGroup courseGroup) {
        if (courseGroup == null) {
            throw new IllegalArgumentException("courseGroup must not be null");
        }
        CourseGroupEntity entity = Objects.requireNonNull(mapToEntity(courseGroup));
        CourseGroupEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public void deleteById(Long groupId) {
        if (groupId != null) {
            jpaRepository.deleteById(groupId);
        }
    }

    private CourseGroup mapToDomain(CourseGroupEntity entity) {
        CourseGroup group = new CourseGroup();
        group.setId(entity.getId());
        group.setLetter(entity.getLetter());
        group.setType(entity.getType());
        group.setMaxCapacity(entity.getMaxCapacity());
        group.setAvailableCapacity(entity.getAvailableCapacity());
        group.setTeacherId(entity.getTeacherId());
        group.setCourseSchedules(mapCourseSchedulesToDomain(entity.getScheduleAssignments()));
        // Enrollments are lazy loaded, so set to empty list to avoid overhead
        // group.setEnrollments(new ArrayList<>());
        // map the owning Course (keep a lightweight Course domain object)
        if (entity.getCourse() != null) {
            Course course = new Course();
            course.setCourseId(entity.getCourse().getCourseId());
            course.setCourseCode(entity.getCourse().getCourseCode());
            course.setName(entity.getCourse().getName());
            course.setCredits(entity.getCourse().getCredits());
            course.setSyllabusId(entity.getCourse().getSyllabusId());
            course.setLabHours(entity.getCourse().getLabHours());
            course.setPracticeHours(entity.getCourse().getPracticeHours());
            course.setTheoryHours(entity.getCourse().getTheoryHours());
            course.setSemesterNumber(entity.getCourse().getSemesterNumber());
            // groups will be loaded by CourseRepository when needed; leave empty here
            group.setCourse(course);
        }
        group.setCourseId(entity.getCourseId());
        return group;
    }

    private List<CourseSchedule> mapCourseSchedulesToDomain(List<CourseScheduleEntity> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return new ArrayList<>();
        }
        List<CourseSchedule> courseSchedules = new ArrayList<>();
        for (CourseScheduleEntity assignment : assignments) {
            CourseSchedule courseSchedule = new CourseSchedule();
            courseSchedule.setId(assignment.getId());
            courseSchedule.setCourseId(assignment.getCourseId());
            courseSchedule.setCourseGroupId(assignment.getCourseGroup() != null ? assignment.getCourseGroup().getId() : null);
            courseSchedule.setClassroomId(assignment.getClassroom() != null ? assignment.getClassroom().getClassroomId() : null);
            courseSchedule.setSequenceOrder(assignment.getSequence());

            ScheduleEntity schedule = assignment.getSchedule();
            if (schedule != null) {
                Schedule slot = new Schedule();
                slot.setId(schedule.getId());
                slot.setDayOfWeek(schedule.getDayOfWeek());
                slot.setStartTime(schedule.getStartTime());
                slot.setEndTime(schedule.getEndTime());
                slot.setScheduleType(ScheduleType.COURSE);
                courseSchedule.setSchedule(slot);
            }
            courseSchedules.add(courseSchedule);
        }
        return courseSchedules;
    }

    private CourseGroupEntity mapToEntity(CourseGroup courseGroup) {
        CourseGroupEntity entity = new CourseGroupEntity();
        entity.setId(courseGroup.getId());
        entity.setLetter(courseGroup.getLetter());
        entity.setType(courseGroup.getType());
        entity.setMaxCapacity(courseGroup.getMaxCapacity());
        entity.setAvailableCapacity(courseGroup.getAvailableCapacity());
        entity.setScheduleAssignments(mapCourseSchedulesToEntity(courseGroup.getCourseSchedules(), entity));
        entity.setTeacherId(courseGroup.getTeacherId());
        // If the domain Course reference exists, set a lightweight CourseEntity reference by id
        if (courseGroup.getCourse() != null && courseGroup.getCourse().getCourseId() != null) {
            CourseEntity courseEntity = new CourseEntity();
            courseEntity.setCourseId(courseGroup.getCourse().getCourseId());
            entity.setCourse(courseEntity);
        } else if (courseGroup.getCourseId() != null) {
            CourseEntity courseEntity = new CourseEntity();
            courseEntity.setCourseId(courseGroup.getCourseId());
            entity.setCourse(courseEntity);
        }
        return entity;
    }

    private List<CourseScheduleEntity> mapCourseSchedulesToEntity(List<CourseSchedule> assignments, CourseGroupEntity courseGroupEntity) {
        List<CourseScheduleEntity> entities = new ArrayList<>();
        if (assignments == null) {
            return entities;
        }
        for (CourseSchedule assignment : assignments) {
            CourseScheduleEntity entity = new CourseScheduleEntity();
            entity.setId(assignment.getId());
            entity.setCourseGroup(courseGroupEntity);
            Long linkedCourseId = assignment.getCourseId();
            if (linkedCourseId == null) {
                if (courseGroupEntity.getCourse() != null) {
                    linkedCourseId = courseGroupEntity.getCourse().getCourseId();
                } else {
                    linkedCourseId = courseGroupEntity.getCourseId();
                }
            }
            entity.setCourseId(linkedCourseId);

            if (assignment.getClassroomId() != null) {
                entity.setClassroom(new ClassroomEntity(assignment.getClassroomId(), null));
            } else {
                entity.setClassroom(null);
            }

            Schedule schedule = assignment.getSchedule();
            ScheduleEntity scheduleEntity = new ScheduleEntity();
            if (schedule != null) {
                scheduleEntity.setId(schedule.getId());
                scheduleEntity.setDayOfWeek(schedule.getDayOfWeek());
                scheduleEntity.setStartTime(schedule.getStartTime());
                scheduleEntity.setEndTime(schedule.getEndTime());
                scheduleEntity.setScheduleType(schedule.getScheduleType() != null ? schedule.getScheduleType() : ScheduleType.COURSE);
            } else {
                scheduleEntity.setScheduleType(ScheduleType.COURSE);
            }
            entity.setSchedule(scheduleEntity);
            entity.setSequence(assignment.getSequenceOrder());

            entities.add(entity);
        }
        return entities;
    }
}