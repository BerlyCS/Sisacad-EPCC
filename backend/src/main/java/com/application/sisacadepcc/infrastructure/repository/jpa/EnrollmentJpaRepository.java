package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentEntity, Long> {
    List<EnrollmentEntity> findByCourseGroup_Id(Long courseGroupId);
    List<EnrollmentEntity> findByStudent_Cui(String studentCui);
    long countByCourseGroup_Id(Long courseGroupId);
    boolean existsByStudent_CuiAndCourseGroup_Id(String studentCui, Long courseGroupId);
}
