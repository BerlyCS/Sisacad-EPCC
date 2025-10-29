package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StudentCourseJpaRepository extends JpaRepository<StudentCourseEntity, Long> {
    List<StudentCourseEntity> findByCourseId(Long courseId);
    List<StudentCourseEntity> findByStudentDocumentoIdentidad(String studentDocumentoIdentidad);

    @Query("SELECT sc FROM StudentCourseEntity sc WHERE sc.studentDocumentoIdentidad = :studentDoc AND sc.courseId = :courseId")
    List<StudentCourseEntity> findByStudentAndCourse(@Param("studentDoc") String studentDocumentoIdentidad,
                                                     @Param("courseId") Long courseId);
}
