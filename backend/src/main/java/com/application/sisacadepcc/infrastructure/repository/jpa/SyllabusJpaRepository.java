package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SyllabusJpaRepository extends JpaRepository<SyllabusEntity, Long> {
    List<Topic> findTopicsById(Long syllabusId);

    Optional<SyllabusEntity> findByCourseCourseId(Long courseId);
}