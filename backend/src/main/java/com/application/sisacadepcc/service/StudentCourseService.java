package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.StudentCourse;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.presentation.dto.StudentScheduleEntry;
import com.application.sisacadepcc.service.dto.EnrollmentValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StudentCourseService {

    private final StudentCourseRepository studentCourseRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final Map<String, Integer> DAY_POSITION = Map.of(
            "LUNES", 1,
            "MARTES", 2,
            "MIERCOLES", 3,
            "JUEVES", 4,
            "VIERNES", 5,
            "SABADO", 6,
            "DOMINGO", 7
    );

    public StudentCourseService(StudentCourseRepository studentCourseRepository,
                                StudentRepository studentRepository,
                                CourseRepository courseRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public List<Student> getStudentsByCourse(Long courseId) {
        List<StudentCourse> enrollments = studentCourseRepository.findByCourseId(courseId);
        List<Student> allStudents = studentRepository.findAll();

        // Crear un mapa de estudiantes por documento de identidad para busqueda rapida
        java.util.Map<String, Student> studentMap = allStudents.stream()
                .collect(Collectors.toMap(Student::getDocumentId, student -> student));

        // Filtrar estudiantes matriculados en el curso
    return enrollments.stream()
        .map(enrollment -> studentMap.get(enrollment.getStudentDocumentoIdentidad()))
        .filter(Objects::nonNull)
        .toList();
    }

    public List<Course> getCoursesByStudent(String studentDocumentoIdentidad) {
        List<StudentCourse> enrollments = studentCourseRepository.findByStudentDocumentoIdentidad(studentDocumentoIdentidad);
        List<Course> allCourses = courseRepository.findAll();

        // Crear un mapa de cursos por ID para busqueda rapida
    java.util.Map<Long, Course> courseMap = allCourses.stream()
        .collect(Collectors.toMap(Course::getCourseId, course -> course));

        // Filtrar cursos en los que el estudiante está matriculado
    return enrollments.stream()
        .map(enrollment -> courseMap.get(enrollment.getCourseId()))
        .filter(Objects::nonNull)
        .toList();
    }

    @Transactional
    public void enrollStudentInCourse(String studentDocumentoIdentidad, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + courseId));

        EnrollmentValidationResult validation = validateCourseEnrollment(studentDocumentoIdentidad, course);
        if (!validation.allowed()) {
            throw new IllegalArgumentException(validation.message());
        }

        StudentCourse enrollment = new StudentCourse(null, studentDocumentoIdentidad, courseId);
        studentCourseRepository.save(enrollment);
    }

    public EnrollmentValidationResult validateLabEnrollment(String studentDocumentoIdentidad, Long labCourseId) {
        if (labCourseId == null) {
            return EnrollmentValidationResult.failure("INVALID_COURSE", "Debe proporcionar un curso válido", null, null);
        }

        Course course = courseRepository.findById(labCourseId)
                .orElse(null);
        if (course == null) {
            return EnrollmentValidationResult.failure("COURSE_NOT_FOUND", "Curso no encontrado", labCourseId, null);
        }

        if (!CourseType.LAB.equals(course.getCourseType())) {
            return EnrollmentValidationResult.failure("NOT_A_LAB", "El curso seleccionado no es un laboratorio", labCourseId, null);
        }

        if (studentDocumentoIdentidad == null || studentDocumentoIdentidad.isBlank()) {
            return EnrollmentValidationResult.failure("INVALID_STUDENT", "No se pudo identificar al estudiante", labCourseId, null);
        }

        return validateCourseEnrollment(studentDocumentoIdentidad.trim(), course);
    }

    @Transactional
    public EnrollmentValidationResult confirmLabEnrollment(String studentDocumentoIdentidad, Long labCourseId) {
        EnrollmentValidationResult validation = validateLabEnrollment(studentDocumentoIdentidad, labCourseId);
        if (!validation.allowed()) {
            return validation;
        }

        try {
            enrollStudentInCourse(studentDocumentoIdentidad, labCourseId);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return EnrollmentValidationResult.failure("ENROLLMENT_FAILED", ex.getMessage(), labCourseId, validation.remainingSeats());
        }

        Course course = courseRepository.findById(labCourseId).orElse(null);
        Integer remainingSeats = course != null ? computeRemainingSeats(course) : validation.remainingSeats();
        return EnrollmentValidationResult.success(labCourseId, remainingSeats);
    }

    public List<StudentCourse> getAllEnrollments() {
        return studentCourseRepository.findAll();
    }

    public List<StudentScheduleEntry> getScheduleForStudent(String studentDocumentoIdentidad) {
        // Excel parser is disabled, so return empty schedule
        return List.of();
    }

    private void ensureNoScheduleConflict(String studentDocumentoIdentidad, Course course) {
        detectScheduleConflict(studentDocumentoIdentidad, course)
                .ifPresent(message -> {
                    throw new IllegalArgumentException(message);
                });
    }

    private java.util.Optional<String> detectScheduleConflict(String studentDocumentoIdentidad, Course course) {
        // Excel parser is disabled, so no schedule conflicts to detect
        return java.util.Optional.empty();
    }

    private EnrollmentValidationResult validateCourseEnrollment(String studentDocumentoIdentidad, Course course) {
        if (studentCourseRepository.existsByStudentAndCourse(studentDocumentoIdentidad, course.getCourseId())) {
            return EnrollmentValidationResult.failure("ALREADY_ENROLLED", "El estudiante ya está matriculado en este curso", course.getCourseId(), computeRemainingSeats(course));
        }

        if (CourseType.LAB.equals(course.getCourseType())) {
            Long theoryCourseId = course.getLabPrerequisiteCourseId();
            if (theoryCourseId == null) {
                return EnrollmentValidationResult.failure("LAB_WITHOUT_THEORY", "El laboratorio no tiene curso teórico configurado", course.getCourseId(), computeRemainingSeats(course));
            }

            boolean hasTheoryEnrollment = studentCourseRepository
                    .existsByStudentAndCourse(studentDocumentoIdentidad, theoryCourseId);
            if (!hasTheoryEnrollment) {
                return EnrollmentValidationResult.failure("MISSING_THEORY", "El estudiante debe estar matriculado en el curso teórico", course.getCourseId(), computeRemainingSeats(course));
            }

            Integer remainingSeats = computeRemainingSeats(course);
            if (remainingSeats != null && remainingSeats <= 0) {
                return EnrollmentValidationResult.failure("LAB_FULL", "No hay vacantes disponibles en el laboratorio", course.getCourseId(), 0);
            }
        }

        java.util.Optional<String> conflict = detectScheduleConflict(studentDocumentoIdentidad, course);
        if (conflict.isPresent()) {
            return EnrollmentValidationResult.failure("SCHEDULE_CONFLICT", conflict.get(), course.getCourseId(), computeRemainingSeats(course));
        }

        return EnrollmentValidationResult.success(course.getCourseId(), computeRemainingSeats(course));
    }

    private Integer computeRemainingSeats(Course course) {
        if (course == null || !CourseType.LAB.equals(course.getCourseType()) || course.getCourseId() == null) {
            return null;
        }
        long enrolledCount = studentCourseRepository.countByCourseId(course.getCourseId());
        int capacity = course.getEffectiveLabCapacity();
        return Math.max(capacity - Math.toIntExact(enrolledCount), 0);
    }

    private boolean isSameDay(String dayA, String dayB) {
        if (dayA == null || dayB == null) {
            return false;
        }
        return dayA.trim().equalsIgnoreCase(dayB.trim());
    }

    private boolean hasTimeOverlap(String startA, String endA, String startB, String endB) {
        LocalTime rangeAStart = parseTime(startA);
        LocalTime rangeAEnd = parseTime(endA);
        LocalTime rangeBStart = parseTime(startB);
        LocalTime rangeBEnd = parseTime(endB);

        if (rangeAStart == null || rangeAEnd == null || rangeBStart == null || rangeBEnd == null) {
            return false;
        }

        // Consider inclusive-exclusive ranges to avoid false positives when one ends exactly when another starts
        return rangeAStart.isBefore(rangeBEnd) && rangeBStart.isBefore(rangeAEnd);
    }

    private LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim();
        try {
            return LocalTime.parse(normalized, TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            try {
                return LocalTime.parse(normalized, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
