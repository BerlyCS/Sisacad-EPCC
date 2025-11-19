package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CourseLookupService {

    private final CourseRepository courseRepository;

    public CourseLookupService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Optional<Course> findByCode(String courseCode) {
        return resolveCourseId(courseCode).flatMap(courseRepository::findById);
    }

    public Optional<Long> resolveCourseId(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(courseCode));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
