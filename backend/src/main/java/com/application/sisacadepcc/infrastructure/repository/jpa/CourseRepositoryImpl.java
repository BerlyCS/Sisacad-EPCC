package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }

    private Course mapToDomain(CourseEntity entity) {
        return new Course(
                entity.getCourseId(),
                entity.getName(),
                entity.getCreditNumber(),
                entity.getGroupLetter(),
                entity.getSyllabusId(),
                entity.getAnio(),  // Agregar el campo anio
                entity.getEnrolledStudentIDs(),
                entity.getTeacherIDs()
        );
    }
}
