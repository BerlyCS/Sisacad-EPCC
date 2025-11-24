package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.presentation.dto.StudentScheduleEntry;
import com.application.sisacadepcc.service.dto.EnrollmentValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentCourseService {

    private final CourseRepository courseRepository;

    public StudentCourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Student> getStudentsByCourse(Long courseId) {
        // Enrollment functionality removed
        return List.of();
    }

    public List<Course> getCoursesByStudent(Long studentId) {
        // Enrollment functionality removed
        return List.of();
    }

    @Transactional
    public void enrollStudentInCourse(Long studentId, Long courseId) {
        // Enrollment functionality removed
    }

    public EnrollmentValidationResult validateLabEnrollment(Long studentId, Long labCourseId) {
        if (labCourseId == null) {
            return EnrollmentValidationResult.failure("INVALID_COURSE", "Debe proporcionar un curso válido", null, null);
        }

        Course course = courseRepository.findById(labCourseId)
                .orElse(null);
        if (course == null) {
            return EnrollmentValidationResult.failure("COURSE_NOT_FOUND", "Curso no encontrado", labCourseId, null);
        }

        // Course type validation removed since courseType field removed
        // if (!CourseType.LAB.equals(null)) {
        //     return EnrollmentValidationResult.failure("NOT_A_LAB", "El curso seleccionado no es un laboratorio", labCourseId, null);
        // }

        if (studentId == null) {
            return EnrollmentValidationResult.failure("INVALID_STUDENT", "No se pudo identificar al estudiante", labCourseId, null);
        }

        return validateCourseEnrollment(studentId, course);
    }

    @Transactional
    public EnrollmentValidationResult confirmLabEnrollment(Long studentId, Long labCourseId) {
        EnrollmentValidationResult validation = validateLabEnrollment(studentId, labCourseId);
        if (!validation.allowed()) {
            return validation;
        }

        try {
            enrollStudentInCourse(studentId, labCourseId);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return EnrollmentValidationResult.failure("ENROLLMENT_FAILED", ex.getMessage(), labCourseId, validation.remainingSeats());
        }

        Course course = courseRepository.findById(labCourseId).orElse(null);
        Integer remainingSeats = course != null ? computeRemainingSeats(course) : validation.remainingSeats();
        return EnrollmentValidationResult.success(labCourseId, remainingSeats);
    }

    public List<?> getAllEnrollments() {
        // Enrollment functionality removed
        return List.of();
    }

    public List<StudentScheduleEntry> getScheduleForStudent(Long studentId) {
        // Excel parser is disabled, so return empty schedule
        return List.of();
    }

    private EnrollmentValidationResult validateCourseEnrollment(Long studentId, Course course) {
        // Enrollment validation removed
        return EnrollmentValidationResult.success(course.getCourseId(), null);
    }

    private Integer computeRemainingSeats(Course course) {
        // Enrollment functionality removed
        return null;
    }
}
