package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;
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
    private final SyllabusRepository syllabusRepository;

    public CourseService(CourseRepository repository,
                         com.application.sisacadepcc.domain.repository.CourseGroupRepository courseGroupRepository,
                         SyllabusRepository syllabusRepository) {
        this.repository = repository;
        this.courseGroupRepository = courseGroupRepository;
        this.syllabusRepository = syllabusRepository;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
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
                    int enrolled = group.getEnrollments() != null ? group.getEnrollments().size() : 0;
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
        Course course = group.getCourse();
        // TODO: Implement enrollment fetching using EnrollmentRepository
        List<Student> students = List.of(); // Placeholder until EnrollmentRepository is implemented

        Course labCourse = null;
        if (group.getType() == CourseType.THEORY && course.getLabPrerequisiteCourseId() != null) {
            labCourse = repository.findById(course.getLabPrerequisiteCourseId()).orElse(null);
        }

        Syllabus syllabus = null;
        if (course.getSyllabusId() != null) {
            syllabus = syllabusRepository.findById(course.getSyllabusId()).orElse(null);
        }

        return new CourseDetails(group, students, labCourse, syllabus);
    }

}
