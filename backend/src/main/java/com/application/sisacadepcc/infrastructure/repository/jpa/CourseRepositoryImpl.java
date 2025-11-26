package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.CourseSchedule;
import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        if (id == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public Course save(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("course must not be null");
        }
        CourseEntity entity = Objects.requireNonNull(mapToEntity(course));
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
        course.setContinuousGradeWeights(extractWeightList(
            entity.getContinuousWeight1(),
            entity.getContinuousWeight2(),
            entity.getContinuousWeight3()
        ));
        course.setExamGradeWeights(extractWeightList(
            entity.getExamWeight1(),
            entity.getExamWeight2(),
            entity.getExamWeight3()
        ));
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
            group.setCourseId(groupEntity.getCourseId());
            group.setCourseSchedules(mapCourseSchedulesToDomain(groupEntity.getScheduleAssignments()));
            groups.add(group);
        }
        return groups;
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
                slot.setScheduleType(com.application.sisacadepcc.domain.model.valueobject.ScheduleType.COURSE);
                courseSchedule.setSchedule(slot);
            }
            courseSchedules.add(courseSchedule);
        }
        return courseSchedules;
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
        List<Integer> continuousWeights = course.getContinuousGradeWeights();
        List<Integer> examWeights = course.getExamGradeWeights();
        entity.setContinuousWeight1(resolveWeight(continuousWeights, 0));
        entity.setContinuousWeight2(resolveWeight(continuousWeights, 1));
        entity.setContinuousWeight3(resolveWeight(continuousWeights, 2));
        entity.setExamWeight1(resolveWeight(examWeights, 0));
        entity.setExamWeight2(resolveWeight(examWeights, 1));
        entity.setExamWeight3(resolveWeight(examWeights, 2));
        // Note: groups mapping may need to be handled separately for persistence
        return entity;
    }

    private List<Integer> extractWeightList(Integer w1, Integer w2, Integer w3) {
        List<Integer> weights = new ArrayList<>(3);
        weights.add(valueOrZero(w1));
        weights.add(valueOrZero(w2));
        weights.add(valueOrZero(w3));
        return weights;
    }

    private Integer resolveWeight(List<Integer> weights, int index) {
        if (weights == null || index < 0 || index >= weights.size()) {
            return 0;
        }
        Integer value = weights.get(index);
        return value != null ? value : 0;
    }

    private Integer valueOrZero(Integer candidate) {
        return candidate != null ? candidate : 0;
    }
}
