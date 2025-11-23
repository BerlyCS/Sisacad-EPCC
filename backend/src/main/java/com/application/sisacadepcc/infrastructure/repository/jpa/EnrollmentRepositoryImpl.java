package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.infrastructure.repository.jpa.EnrollmentEntity;
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
    public List<EnrollmentEntity> findAll() {
        return jpaRepository.findAll().stream()
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentEntity> findByCourseGroupId(Long courseGroupId) {
        return jpaRepository.findByCourseGroup_Id(courseGroupId).stream()
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentEntity> findByStudentCui(String studentCui) {
        return jpaRepository.findByStudent_Cui(studentCui).stream()
                .collect(Collectors.toList());
    }

    @Override
    public void save(EnrollmentEntity studentCourse) {
        jpaRepository.save(studentCourse);
    }

    @Override
    public void saveAll(List<EnrollmentEntity> studentCourses) {
        jpaRepository.saveAll(studentCourses);
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
}
