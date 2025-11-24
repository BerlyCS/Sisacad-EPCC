package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {
    private final GradeRepository repository;

    public GradeService(GradeRepository repository) {
        this.repository = repository;
    }

    public List<Grade> getAllGrades() {
        return repository.findAll();
    }

    public Optional<Grade> findByID(Long gradeID) {
        return repository.findById(gradeID);
    }

    public Optional<Grade> findByCourseAndStudent(String courseCode, Long studentId) {
        return repository.findByCourseAndStudent(courseCode, studentId);
    }

    public List<Grade> findByStudent(Long studentId) {
        return repository.findByStudent(studentId);
    }

    public List<Grade> findByCourseCode(String courseCode) {
        return repository.findByCourseCode(courseCode);
    }

    public boolean save(Grade grade) {
        return repository.save(grade);
    }

    public boolean deleteById(Long gradeID) {
        return repository.deleteById(gradeID);
    }

}
