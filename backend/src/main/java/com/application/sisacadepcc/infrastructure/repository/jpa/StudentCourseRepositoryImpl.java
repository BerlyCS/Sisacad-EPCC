package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.StudentCourse;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class StudentCourseRepositoryImpl implements StudentCourseRepository {

    private final StudentCourseJpaRepository jpaRepository;

    public StudentCourseRepositoryImpl(StudentCourseJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<StudentCourse> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentCourse> findByCourseId(Long courseId) {
        return jpaRepository.findByCourseId(courseId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentCourse> findByStudentDocumentoIdentidad(String studentDocumentoIdentidad) {
        return jpaRepository.findByStudentDocumentoIdentidad(studentDocumentoIdentidad).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(StudentCourse studentCourse) {
        StudentCourseEntity entity = toEntity(studentCourse);
        jpaRepository.save(entity);
    }

    @Override
    public void saveAll(List<StudentCourse> studentCourses) {
        List<StudentCourseEntity> entities = studentCourses.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        jpaRepository.saveAll(entities);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public boolean existsByStudentAndCourse(String studentDocumentoIdentidad, Long courseId) {
        return !jpaRepository.findByStudentAndCourse(studentDocumentoIdentidad, courseId).isEmpty();
    }

    private StudentCourse toDomain(StudentCourseEntity entity) {
        return new StudentCourse(
                entity.getId(),
                entity.getStudentDocumentoIdentidad(),
                entity.getCourseId()
        );
    }

    private StudentCourseEntity toEntity(StudentCourse domain) {
        return new StudentCourseEntity(
                domain.getStudentDocumentoIdentidad(),
                domain.getCourseId()
        );
    }
}
