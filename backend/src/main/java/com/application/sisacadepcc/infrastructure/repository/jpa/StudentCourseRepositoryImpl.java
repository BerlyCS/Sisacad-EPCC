package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.StudentCourse;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class StudentCourseRepositoryImpl implements StudentCourseRepository {

    // Temporary implementation to allow application startup
    // TODO: Implement proper JPA-based repository

    @Override
    public List<StudentCourse> findAll() {
        return new ArrayList<>();
    }

    @Override
    public List<StudentCourse> findByCourseId(Long courseId) {
        return new ArrayList<>();
    }

    @Override
    public List<StudentCourse> findByStudentDocumentoIdentidad(String studentDocumentoIdentidad) {
        return new ArrayList<>();
    }

    @Override
    public StudentCourse save(StudentCourse enrollment) {
        // Return the enrollment as-is for now
        return enrollment;
    }

    @Override
    public boolean existsByStudentAndCourse(String studentDocumentoIdentidad, Long courseId) {
        return false;
    }

    @Override
    public long countByCourseId(Long courseId) {
        return 0;
    }
}