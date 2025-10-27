package com.application.sisacadepcc.domain.repository;

import java.util.List;

import com.application.sisacadepcc.domain.model.valueobject.Topic;
import org.springframework.stereotype.Repository;

import com.application.sisacadepcc.domain.model.Syllabus;

@Repository
public interface SyllabusRepository {

    List<Syllabus> findAll();

    List<Topic> getTopics(Long syllabusId);
}
