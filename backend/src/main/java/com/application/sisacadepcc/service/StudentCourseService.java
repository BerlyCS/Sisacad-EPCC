package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.StudentCourse;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.presentation.dto.StudentScheduleEntry;
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
    private final ExcelScheduleService excelScheduleService;
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
                                CourseRepository courseRepository,
                                ExcelScheduleService excelScheduleService) {
        this.studentCourseRepository = studentCourseRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.excelScheduleService = excelScheduleService;
    }

    public List<Student> getStudentsByCourse(Long courseId) {
        List<StudentCourse> enrollments = studentCourseRepository.findByCourseId(courseId);
        List<Student> allStudents = studentRepository.findAll();

        // Crear un mapa de estudiantes por documento de identidad para busqueda rapida
        java.util.Map<String, Student> studentMap = allStudents.stream()
                .collect(Collectors.toMap(Student::getDocumentoIdentidad, student -> student));

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

        if (studentCourseRepository.existsByStudentAndCourse(studentDocumentoIdentidad, courseId)) {
            throw new IllegalArgumentException("El estudiante ya está matriculado en este curso");
        }

        if (CourseType.LAB.equals(course.getCourseType())) {
            Long theoryCourseId = course.getLabPrerequisiteCourseId();
            if (theoryCourseId == null) {
                throw new IllegalStateException("El laboratorio no tiene curso teórico configurado");
            }
            boolean hasTheoryEnrollment = studentCourseRepository
                    .existsByStudentAndCourse(studentDocumentoIdentidad, theoryCourseId);
            if (!hasTheoryEnrollment) {
                throw new IllegalArgumentException("El estudiante no está matriculado en el curso teórico requerido");
            }
        }

        ensureNoScheduleConflict(studentDocumentoIdentidad, course);

        StudentCourse enrollment = new StudentCourse(null, studentDocumentoIdentidad, courseId);
        studentCourseRepository.save(enrollment);
    }

    public List<StudentCourse> getAllEnrollments() {
        return studentCourseRepository.findAll();
    }

    public List<StudentScheduleEntry> getScheduleForStudent(String studentDocumentoIdentidad) {
        List<Course> courses = getCoursesByStudent(studentDocumentoIdentidad);
        if (courses.isEmpty()) {
            return List.of();
        }

        List<StudentScheduleEntry> entries = new ArrayList<>();

        for (Course course : courses) {
            String group = (course.getGroupLetter() == '\0') ? null : String.valueOf(course.getGroupLetter());
            List<ExcelScheduleService.OccupiedTimeSlot> slots = excelScheduleService
                .findByCourse(course.getName(), group, course.getCourseType());
            for (ExcelScheduleService.OccupiedTimeSlot slot : slots) {
                entries.add(new StudentScheduleEntry(
                        course.getCourseId(),
                        course.getCourseCode(),
                        course.getName(),
                        course.getCourseType(),
                        slot.getDayOfWeek(),
                        slot.getStartTime(),
                        slot.getEndTime(),
                        slot.getClassroomName()
                ));
            }
        }

        Comparator<StudentScheduleEntry> comparator = Comparator
                .comparing((StudentScheduleEntry entry) -> DAY_POSITION.getOrDefault(entry.getDayOfWeek().toUpperCase(Locale.ROOT), Integer.MAX_VALUE))
                .thenComparing(StudentScheduleEntry::getStartTime)
                .thenComparing(StudentScheduleEntry::getCourseName);

        entries.sort(comparator);
        return entries;
    }

    private void ensureNoScheduleConflict(String studentDocumentoIdentidad, Course course) {
        List<StudentScheduleEntry> currentSchedule = getScheduleForStudent(studentDocumentoIdentidad);
        if (currentSchedule.isEmpty()) {
            return;
        }

        String group = (course.getGroupLetter() == '\0') ? null : String.valueOf(course.getGroupLetter());
        List<ExcelScheduleService.OccupiedTimeSlot> targetSlots = excelScheduleService
            .findByCourse(course.getName(), group, course.getCourseType());
        if (targetSlots == null || targetSlots.isEmpty()) {
            return;
        }

        for (ExcelScheduleService.OccupiedTimeSlot slot : targetSlots) {
            for (StudentScheduleEntry entry : currentSchedule) {
                if (isSameDay(slot.getDayOfWeek(), entry.getDayOfWeek()) &&
                        hasTimeOverlap(slot.getStartTime(), slot.getEndTime(), entry.getStartTime(), entry.getEndTime())) {
                    throw new IllegalArgumentException(String.format(
                            "El horario del curso %s (%s-%s) se superpone con %s (%s-%s)",
                            course.getName(), slot.getStartTime(), slot.getEndTime(),
                            entry.getCourseName(), entry.getStartTime(), entry.getEndTime()
                    ));
                }
            }
        }
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
