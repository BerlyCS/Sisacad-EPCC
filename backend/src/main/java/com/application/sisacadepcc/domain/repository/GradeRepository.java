package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Grade;

import java.util.List;
import java.util.Optional;

public interface GradeRepository {

    List<Grade> findAll();

    Optional<Grade> findById(Long gradeID);

    Optional<Grade> findByCourseAndStudent(Long courseId, Long studentId);

    List<Grade> findByStudent(Long studentId);

    List<Grade> findByCourseId(Long courseId);

    boolean save(Grade grade);

    boolean deleteById(Long gradeID);
}
