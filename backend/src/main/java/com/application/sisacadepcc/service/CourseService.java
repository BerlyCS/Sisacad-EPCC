package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.EnrollmentRepository;
import com.application.sisacadepcc.service.dto.CourseDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository repository;
    private final com.application.sisacadepcc.domain.repository.CourseGroupRepository courseGroupRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseService(CourseRepository repository,
                         com.application.sisacadepcc.domain.repository.CourseGroupRepository courseGroupRepository,
                         EnrollmentRepository enrollmentRepository) {
        this.repository = repository;
        this.courseGroupRepository = courseGroupRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Course createCourse(Course course) {
        return repository.save(course);
    }

    public List<Course> getCoursesForProfessor(Long professorId) {
        if (professorId == null) {
            return List.of();
        }

        return courseGroupRepository.findByTeacherId(professorId).stream()
                .map(CourseGroup::getCourse)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public Optional<CourseDetails> getCourseDetails(Long groupId) {
        if (groupId == null) {
            return Optional.empty();
        }

        return courseGroupRepository.findById(groupId)
                .map(this::buildCourseDetails);
    }

    public List<CourseGroup> getLabGroupsForCourse(Long courseId) {
        if (courseId == null) {
            return List.of();
        }
        return courseGroupRepository.findByCourseId(courseId).stream()
                .filter(group -> group.getType() == CourseType.LAB)
                .collect(Collectors.toList());
    }

    public Optional<CourseGroup> updateGroupCapacity(Long groupId, Integer capacity) {
        if (groupId == null || capacity == null || capacity < 0) {
            return Optional.empty();
        }
        return courseGroupRepository.findById(groupId)
                .map(group -> {
                    group.setMaxCapacity(capacity);
                    int enrolled = (int) enrollmentRepository.countByCourseGroupId(group.getId());
                    group.setAvailableCapacity(Math.max(0, capacity - enrolled));
                    return courseGroupRepository.save(group);
                });
    }

    /**
     * Return all distinct teacher IDs assigned to course groups.
     * Useful for administrative UIs and quick validation.
     */
    public java.util.List<Long> getAllGroupTeacherIds() {
        return courseGroupRepository.findAll().stream()
            .map(CourseGroup::getTeacherId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    public Optional<Course> assignProfessorToCourse(Long courseId, Long professorId) {
        // TODO: Implement assignment logic using CourseGroupRepository
        return Optional.empty();
    }

    public Optional<Course> removeProfessorFromCourse(Long courseId, Long professorId) {
        // TODO: Implement removal logic using CourseGroupRepository
        return Optional.empty();
    }

    private CourseDetails buildCourseDetails(CourseGroup group) {
        return new CourseDetails(group, List.of(), null, null);
    }

}
