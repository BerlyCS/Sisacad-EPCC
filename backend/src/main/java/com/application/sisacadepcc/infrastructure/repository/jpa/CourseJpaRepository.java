package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CourseJpaRepository extends JpaRepository<CourseEntity, Long> {

    Optional<CourseEntity> findByCourseCode(int courseCode);

    // Add methods as needed for the new schema
}
