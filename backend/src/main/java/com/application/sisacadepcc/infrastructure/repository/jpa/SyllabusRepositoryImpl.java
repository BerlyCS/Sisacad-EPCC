package com.application.sisacadepcc.infrastructure.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.application.sisacadepcc.domain.model.valueobject.Topic;
import org.springframework.stereotype.Repository;

import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;

@Repository
public class SyllabusRepositoryImpl implements SyllabusRepository {
    private final SyllabusJpaRepository jpaRepository;

    public SyllabusRepositoryImpl(SyllabusJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Syllabus> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Optional<Syllabus> findById(Long syllabusId) {
        if (syllabusId == null) {
            return Optional.empty();
        }

        return jpaRepository.findById(syllabusId)
                .map(this::mapToDomain);
    }

    private Syllabus mapToDomain(SyllabusEntity entity) {
        return new Syllabus(
                entity.getId(),
                entity.getCourse().getCourseId(),
                entity.getContent(),
                entity.getTopics()
        );
    }

    @Override
    public List<Topic> getTopics(Long syllabusId) {
        return jpaRepository.findTopicsById(syllabusId);
    }

}