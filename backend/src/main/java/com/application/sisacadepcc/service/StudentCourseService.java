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

        if (!studentCourseRepository.existsByStudentAndCourse(studentDocumentoIdentidad, courseId)) {
            StudentCourse enrollment = new StudentCourse(null, studentDocumentoIdentidad, courseId);
            studentCourseRepository.save(enrollment);
        }
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
            List<ExcelScheduleService.OccupiedTimeSlot> slots = excelScheduleService.findByCourseName(course.getName());
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
}
