package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.valueobject.CourseScheduleSlot;
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
    public List<Course> findByAnio(Integer anio) {
        return jpaRepository.findByAnio(anio)
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Course> findByLabPrerequisiteCourseId(Long theoryCourseId) {
        if (theoryCourseId == null) {
            return List.of();
        }
        return jpaRepository.findByLabPrerequisiteCourseId(theoryCourseId)
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Optional<Course> updateLabCapacity(Long courseId, Integer labCapacity) {
        if (courseId == null) {
            return Optional.empty();
        }

        return jpaRepository.findById(courseId)
                .map(entity -> {
                    entity.setLabCapacity(labCapacity);
                    CourseEntity saved = jpaRepository.save(entity);
                    return mapToDomain(saved);
                });
    }

    private Course mapToDomain(CourseEntity entity) {
        Course course = new Course();
        course.setCourseId(entity.getCourseId());
        course.setCourseCode(entity.getCourseId());
        course.setName(entity.getName());
        course.setCreditNumber(entity.getCreditNumber());
        course.setGroupLetter(entity.getGroupLetter());
        course.setSyllabusID(entity.getSyllabusId());
        course.setAnio(entity.getAnio());
        course.setCourseType(entity.getCourseType());
        course.setLabPrerequisiteCourseId(entity.getLabPrerequisiteCourseId());
        course.setLabCapacity(entity.getLabCapacity());
        List<Long> enrolledIds = entity.getEnrolledStudentIDs() != null
            ? entity.getEnrolledStudentIDs()
            : List.of();
        List<Long> teacherIds = entity.getTeacherIDs() != null
            ? entity.getTeacherIDs()
            : List.of();
        course.setEnrolledStudentIDs(new ArrayList<>(enrolledIds));
        course.setTeacherIDs(new ArrayList<>(teacherIds));
        course.setContinuousGradeWeights(entity.getContinuousGradeWeights());
        course.setExamGradeWeights(entity.getExamGradeWeights());
        course.setLabCapacity(entity.getLabCapacity());
        course.setScheduleSlots(mapScheduleSlotsToDomain(entity.getScheduleSlots()));
        return course;
    }

    private List<CourseScheduleSlot> mapScheduleSlotsToDomain(List<ScheduleSlotEmbeddable> embeddables) {
        if (embeddables == null || embeddables.isEmpty()) {
            return new ArrayList<>();
        }
        List<CourseScheduleSlot> slots = new ArrayList<>();
        for (ScheduleSlotEmbeddable embeddable : embeddables) {
            CourseScheduleSlot slot = new CourseScheduleSlot(
                    embeddable.getClassroomName(),
                    embeddable.getDayOfWeek(),
                    embeddable.getStartTime(),
                    embeddable.getEndTime()
            );
            slots.add(slot);
        }
        return slots;
    }

    private List<ScheduleSlotEmbeddable> mapScheduleSlotsToEmbeddable(List<CourseScheduleSlot> slots) {
        List<ScheduleSlotEmbeddable> embeddables = new ArrayList<>();
        if (slots == null) {
            return embeddables;
        }
        for (CourseScheduleSlot slot : slots) {
            ScheduleSlotEmbeddable embeddable = new ScheduleSlotEmbeddable();
            embeddable.setClassroomName(slot.getClassroomName());
            embeddable.setDayOfWeek(slot.getDayOfWeek());
            embeddable.setStartTime(slot.getStartTime());
            embeddable.setEndTime(slot.getEndTime());
            embeddables.add(embeddable);
        }
        return embeddables;
    }

    private CourseEntity mapToEntity(Course course) {
        CourseEntity entity = new CourseEntity();
        entity.setCourseId(course.getCourseId());
        entity.setName(course.getName());
        entity.setCreditNumber(course.getCreditNumber());
        entity.setGroupLetter(course.getGroupLetter());
        entity.setSyllabusId(course.getSyllabusID());
        entity.setAnio(course.getAnio());
        entity.setCourseType(course.getCourseType());
        entity.setLabPrerequisiteCourseId(course.getLabPrerequisiteCourseId());
        entity.setLabCapacity(course.getLabCapacity());
        entity.setEnrolledStudentIDs(new ArrayList<>(course.getEnrolledStudentIDs()));
        entity.setTeacherIDs(new ArrayList<>(course.getTeacherIDs()));
        entity.setContinuousGradeWeights(course.getContinuousGradeWeights());
        entity.setExamGradeWeights(course.getExamGradeWeights());
        entity.setScheduleSlots(mapScheduleSlotsToEmbeddable(course.getScheduleSlots()));
        return entity;
    }

    @Override
    public Optional<Course> updateTeacherAssignments(Long courseId, List<Long> teacherIds) {
        if (courseId == null) {
            return Optional.empty();
        }

        return jpaRepository.findById(courseId)
                .map(entity -> {
                    List<Long> normalized = teacherIds != null
                            ? new ArrayList<>(teacherIds)
                            : new ArrayList<>();
                    entity.setTeacherIDs(normalized);
                    CourseEntity saved = jpaRepository.save(entity);
                    return mapToDomain(saved);
                });
    }

    @Override
    public Course save(Course course) {
        CourseEntity entity = mapToEntity(course);
        CourseEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public void deleteById(Long courseId) {
        if (courseId != null) {
            jpaRepository.deleteById(courseId);
        }
    }
}
