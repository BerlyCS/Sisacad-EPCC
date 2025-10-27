package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusJpaRepository extends JpaRepository<SyllabusEntity, Long> {
    List<Topic> findTopicsById(Long syllabusId);
}