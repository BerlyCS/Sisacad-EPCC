package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Grade;

import java.util.List;
import java.util.Optional;

public interface GradeRepository {

    List<Grade> findAll();

    Optional<Grade> findById(Long gradeID);

    Optional<Grade> findByCourseAndStudent(String courseCode, String studentDocumentoIdentidad);

    List<Grade> findByStudentDocumento(String studentDocumentoIdentidad);

    List<Grade> findByCourseCode(String courseCode);

    boolean save(Grade grade);

    boolean deleteById(Long gradeID);
}
