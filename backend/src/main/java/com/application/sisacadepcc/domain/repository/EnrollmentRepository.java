package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.infrastructure.repository.jpa.EnrollmentEntity;
import java.util.List;

public interface EnrollmentRepository {
    List<EnrollmentEntity> findAll();
    List<EnrollmentEntity> findByCourseGroupId(Long courseGroupId);
    List<EnrollmentEntity> findByStudentCui(String studentCui);
    void save(EnrollmentEntity studentCourse);
    void saveAll(List<EnrollmentEntity> studentCourses);
    void deleteAll();
    boolean existsByStudentAndCourseGroup(String studentCui, Long courseGroupId);
    long countByCourseGroupId(Long courseGroupId);
}
