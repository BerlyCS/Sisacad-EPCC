package com.application.sisacadepcc.infrastructure.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.application.sisacadepcc.domain.model.valueobject.Topic;
import org.springframework.stereotype.Repository;

import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;

import com.application.sisacadepcc.infrastructure.repository.jpa.CourseEntity;

@Repository
public class SyllabusRepositoryImpl implements SyllabusRepository {
    private final SyllabusJpaRepository jpaRepository;
    private final CourseJpaRepository courseJpaRepository;

    public SyllabusRepositoryImpl(SyllabusJpaRepository jpaRepository,
                                  CourseJpaRepository courseJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.courseJpaRepository = courseJpaRepository;
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

    @Override
    public Optional<Syllabus> findByCourseId(Long courseId) {
        if (courseId == null) {
            return Optional.empty();
        }
        return jpaRepository.findByCourseCourseId(courseId)
                .map(this::mapToDomain);
    }

    @Override
    public Syllabus save(Syllabus syllabus) {
        if (syllabus == null || syllabus.getCourseId() == null) {
            throw new IllegalArgumentException("Syllabus and courseId are required");
        }

        CourseEntity course = courseJpaRepository.findById(syllabus.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found for syllabus"));

        SyllabusEntity entity = mapToEntity(syllabus, course);
        SyllabusEntity saved = jpaRepository.save(entity);

        // Keep bidirectional reference in sync
        course.setSyllabusId(saved.getId());
        courseJpaRepository.save(course);

        return mapToDomain(saved);
    }

    @Override
    public void deleteById(Long syllabusId) {
        if (syllabusId == null) {
            return;
        }

        jpaRepository.findById(syllabusId).ifPresent(entity -> {
            CourseEntity course = entity.getCourse();
            if (course != null && syllabusId.equals(course.getSyllabusId())) {
                course.setSyllabusId(null);
                courseJpaRepository.save(course);
            }
            jpaRepository.delete(entity);
        });
    }

    private Syllabus mapToDomain(SyllabusEntity entity) {
        return new Syllabus(
                entity.getId(),
                entity.getCourse().getCourseId(),
                entity.getContent(),
                entity.getTopics()
        );
    }

    private SyllabusEntity mapToEntity(Syllabus syllabus, CourseEntity course) {
        SyllabusEntity entity = new SyllabusEntity();
        entity.setId(syllabus.getId());
        entity.setCourse(course);
        entity.setContent(syllabus.getContent());
        entity.setTopics(syllabus.getTopics());
        return entity;
    }

    @Override
    public List<Topic> getTopics(Long syllabusId) {
        return jpaRepository.findTopicsById(syllabusId);
    }

}