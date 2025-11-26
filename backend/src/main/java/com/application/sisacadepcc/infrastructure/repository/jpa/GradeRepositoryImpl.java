package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class GradeRepositoryImpl implements GradeRepository {

    private final GradeJpaRepository jpaRepository;

    public GradeRepositoryImpl(GradeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Grade> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Grade> findById(Long gradeID) {
        return jpaRepository.findById(gradeID)
                .map(this::mapToDomain);
    }

    @Override
    public Optional<Grade> findByCourseAndStudent(Long courseId, Long studentId) {
        return jpaRepository.findByCourseIdAndStudentId(courseId, studentId)
                .map(this::mapToDomain);
    }

    @Override
    public List<Grade> findByStudent(Long studentId) {
        return jpaRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Grade> findByCourseId(Long courseId) {
        return jpaRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean save(Grade grade) {
        try {
            jpaRepository.save(mapToEntity(grade));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteById(Long gradeID) {
        if (jpaRepository.existsById(gradeID)) {
            jpaRepository.deleteById(gradeID);
            return true;
        }
        return false;
    }

    // =========================
    // Mapping helpers
    // =========================

    private Grade mapToDomain(GradeEntity entity) {
        return new Grade(
                entity.getGradeID(),
                entity.getStudentId(),
                entity.getCourseId(),
                entity.getProfessorID(),
                entity.getContinuousGrades(),
                entity.getExamGrades()
        );
    }

    private GradeEntity mapToEntity(Grade domain) {
        GradeEntity entity = new GradeEntity();
        entity.setGradeID(domain.getGradeID());
        entity.setStudentId(domain.getStudentId());
        entity.setCourseId(domain.getCourseId());
        entity.setProfessorID(domain.getProfessorID());
        entity.setContinuousGrades(new java.util.ArrayList<>(domain.getContinuousGrades()));
        entity.setExamGrades(new java.util.ArrayList<>(domain.getExamGrades()));
        return entity;
    }
}
