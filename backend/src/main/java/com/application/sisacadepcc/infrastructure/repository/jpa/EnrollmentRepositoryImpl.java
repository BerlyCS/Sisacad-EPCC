package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.StudentCourse;
import com.application.sisacadepcc.domain.repository.EnrollmentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class EnrollmentRepositoryImpl implements EnrollmentRepository {

    private final EnrollmentJpaRepository jpaRepository;

    public EnrollmentRepositoryImpl(EnrollmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<StudentCourse> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentCourse> findByCourseGroupId(Long courseGroupId) {
        return jpaRepository.findByCourseGroup_Id(courseGroupId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentCourse> findByStudentCui(String studentCui) {
        return jpaRepository.findByStudent_Cui(studentCui).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(StudentCourse studentCourse) {
        EnrollmentEntity entity = toEntity(studentCourse);
        jpaRepository.save(entity);
    }

    @Override
    public void saveAll(List<StudentCourse> studentCourses) {
        List<EnrollmentEntity> entities = studentCourses.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        jpaRepository.saveAll(entities);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public boolean existsByStudentAndCourseGroup(String studentCui, Long courseGroupId) {
        return jpaRepository.existsByStudent_CuiAndCourseGroup_Id(studentCui, courseGroupId);
    }

    @Override
    public long countByCourseGroupId(Long courseGroupId) {
        if (courseGroupId == null) {
            return 0;
        }
        return jpaRepository.countByCourseGroup_Id(courseGroupId);
    }

    private StudentCourse toDomain(EnrollmentEntity entity) {
        return new StudentCourse(
                entity.getId(),
                entity.getStudent().getCui(),
                entity.getCourseGroup().getId()
        );
    }

    private EnrollmentEntity toEntity(StudentCourse domain) {
        // Assume student and courseGroup are fetched by IDs
        // For now, create with null, to be set in service
        EnrollmentEntity entity = new EnrollmentEntity(null, null);
        entity.setId(domain.getId());
        return entity;
    }
}
