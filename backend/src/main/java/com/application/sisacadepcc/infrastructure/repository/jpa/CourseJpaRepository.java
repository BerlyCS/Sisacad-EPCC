package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseJpaRepository extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findByAnio(Integer anio);
}
