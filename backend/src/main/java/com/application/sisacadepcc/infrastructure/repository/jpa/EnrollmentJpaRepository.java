package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentEntity, Long> {
    List<EnrollmentEntity> findByCourseGroupId(Long courseGroupId);
    List<EnrollmentEntity> findByStudentCui(String studentCui);
    long countByCourseGroupId(Long courseGroupId);

    @Query("SELECT e FROM EnrollmentEntity e WHERE e.student.cui = :studentCui AND e.courseGroup.id = :courseGroupId")
    List<EnrollmentEntity> findByStudentAndCourseGroup(@Param("studentCui") String studentCui,
                                                       @Param("courseGroupId") Long courseGroupId);
}
