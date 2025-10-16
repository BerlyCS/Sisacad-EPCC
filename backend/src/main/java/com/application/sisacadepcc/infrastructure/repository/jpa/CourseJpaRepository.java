package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.infrastructure.repository.jpa.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseJpaRepository extends JpaRepository<CourseEntity, Long> {
    // JpaRepository already provides findAll(), findById(), save(), delete(), etc.
}
