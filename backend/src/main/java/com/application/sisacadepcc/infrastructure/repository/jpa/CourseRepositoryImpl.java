package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Course;
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
        return course;
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
}
