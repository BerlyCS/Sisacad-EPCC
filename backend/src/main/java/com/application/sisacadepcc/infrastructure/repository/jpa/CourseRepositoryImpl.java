package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import org.springframework.stereotype.Repository;

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
    course.setCourseID(entity.getCourseId());
    course.setName(entity.getName());
    course.setCreditNumber(entity.getCreditNumber());
    course.setGroupLetter(entity.getGroupLetter());
    course.setSyllabusID(entity.getSyllabusId());
    course.setAnio(entity.getAnio());
    course.setCourseType(entity.getCourseType());
    course.setLabPrerequisiteCourseId(entity.getLabPrerequisiteCourseId());
    course.setEnrolledStudentIDs(entity.getEnrolledStudentIDs());
    course.setTeacherIDs(entity.getTeacherIDs());
    return course;
    }
}
