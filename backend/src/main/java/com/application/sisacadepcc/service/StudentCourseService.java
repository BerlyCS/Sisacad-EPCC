package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.domain.repository.CourseGroupRepository;
import com.application.sisacadepcc.domain.repository.EnrollmentRepository;
import com.application.sisacadepcc.infrastructure.repository.jpa.EnrollmentEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.StudentEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.StudentJpaRepository;
import com.application.sisacadepcc.infrastructure.repository.jpa.CourseGroupJpaRepository;
import com.application.sisacadepcc.presentation.dto.StudentScheduleEntry;
import com.application.sisacadepcc.service.dto.EnrollmentValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StudentCourseService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentJpaRepository studentJpaRepository;
    private final CourseGroupJpaRepository courseGroupJpaRepository;

    public StudentCourseService(CourseRepository courseRepository,
                                StudentRepository studentRepository,
                                CourseGroupRepository courseGroupRepository,
                                EnrollmentRepository enrollmentRepository,
                                StudentJpaRepository studentJpaRepository,
                                CourseGroupJpaRepository courseGroupJpaRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.courseGroupRepository = courseGroupRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentJpaRepository = studentJpaRepository;
        this.courseGroupJpaRepository = courseGroupJpaRepository;
    }

    public List<Student> getStudentsByCourse(Long courseId) {
        // Enrollment functionality removed
        return List.of();
    }

    public List<Course> getCoursesByStudent(Long studentId) {
        Student student = resolveStudent(studentId);
        if (student == null || student.getCui() == null) {
            return List.of();
        }

        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudentCui(student.getCui());
        if (enrollments.isEmpty()) {
            return List.of();
        }

        Map<Long, Course> coursesById = new LinkedHashMap<>();

        for (EnrollmentEntity enrollment : enrollments) {
            Long courseGroupId = enrollment.getCourseGroup() != null ? enrollment.getCourseGroup().getId() : null;
            if (courseGroupId == null) {
                continue;
            }

            CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId).orElse(null);
            if (courseGroup == null) {
                continue;
            }

            Course course = resolveCourse(courseGroup);
            if (course == null || course.getCourseId() == null) {
                continue;
            }

            Course summaryCourse = coursesById.computeIfAbsent(course.getCourseId(), id -> createCourseSummary(course));

            boolean alreadyAdded = summaryCourse.getGroups().stream()
                    .anyMatch(group -> Objects.equals(group.getId(), courseGroup.getId()));
            if (alreadyAdded) {
                continue;
            }

            CourseGroup summaryGroup = createCourseGroupSummary(courseGroup, summaryCourse);
            summaryCourse.getGroups().add(summaryGroup);
        }

        return new ArrayList<>(coursesById.values());
    }

    @Transactional
    public void enrollStudentInCourse(Long studentId, Long courseId) {
        // Enrollment functionality removed
    }

    @Transactional
    public void enrollStudentInCourseGroups(Long studentId, List<Long> courseGroupIds) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID is required");
        }

        List<Long> targets = courseGroupIds == null
                ? List.of()
                : courseGroupIds.stream().filter(Objects::nonNull).distinct().toList();

        if (targets.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un grupo válido");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        var studentEntity = studentJpaRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student entity not found"));

        for (Long courseGroupId : targets) {
            enrollInSingleGroup(student, studentEntity, courseGroupId);
        }
    }

    @Transactional
    public void enrollStudentInCourseGroup(Long studentId, Long courseGroupId) {
        enrollStudentInCourseGroups(studentId, courseGroupId != null ? List.of(courseGroupId) : List.of());
    }

    private void enrollInSingleGroup(Student student, StudentEntity studentEntity, Long courseGroupId) {
        if (courseGroupId == null) {
            throw new IllegalArgumentException("Course group ID is required");
        }

        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Course group not found"));

        if (courseGroup.getType() == CourseType.LAB) {
            throw new IllegalArgumentException("Los laboratorios deben gestionarse desde el módulo de estudiantes");
        }

        boolean alreadyEnrolled = enrollmentRepository.findByCourseGroupId(courseGroupId).stream()
                .anyMatch(enrollment -> enrollment.getStudent().getCui().equals(student.getCui()));

        if (alreadyEnrolled) {
            throw new IllegalStateException("El estudiante ya está matriculado en este grupo");
        }

        long currentEnrollments = enrollmentRepository.countByCourseGroupId(courseGroupId);
        if (currentEnrollments >= courseGroup.getMaxCapacity()) {
            throw new IllegalStateException("El grupo ha alcanzado su capacidad máxima");
        }

        var courseGroupEntity = courseGroupJpaRepository.findById(courseGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Course group entity not found"));

        EnrollmentEntity enrollment = new EnrollmentEntity(studentEntity, courseGroupEntity);
        enrollmentRepository.save(enrollment);
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
        Student student = resolveStudent(studentId);
        if (student == null || student.getCui() == null) {
            return List.of();
        }

        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudentCui(student.getCui());
        if (enrollments.isEmpty()) {
            return List.of();
        }

        List<StudentScheduleEntry> scheduleEntries = new ArrayList<>();

        for (EnrollmentEntity enrollment : enrollments) {
            Long courseGroupId = enrollment.getCourseGroup() != null ? enrollment.getCourseGroup().getId() : null;
            if (courseGroupId == null) {
                continue;
            }

            CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId).orElse(null);
            if (courseGroup == null) {
                continue;
            }

            Course course = resolveCourse(courseGroup);
            if (course == null) {
                continue;
            }

            courseGroup.getCourseSchedules().stream()
                    .filter(courseSchedule -> courseSchedule.getSchedule() != null)
                    .sorted(Comparator.comparing(courseSchedule -> {
                        if (courseSchedule.getSchedule() == null || courseSchedule.getSchedule().getStartTime() == null) {
                            return "";
                        }
                        return courseSchedule.getSchedule().getStartTime().toString();
                    }))
                    .forEach(courseSchedule -> scheduleEntries.add(new StudentScheduleEntry(
                            course.getCourseId(),
                            course.getCourseCode() != null ? course.getCourseCode().longValue() : null,
                            course.getName(),
                            courseGroup.getType(),
                            courseSchedule.getSchedule().getDayOfWeek(),
                            formatTime(courseSchedule.getSchedule().getStartTime()),
                            formatTime(courseSchedule.getSchedule().getEndTime()),
                            resolveClassroomName(courseSchedule.getClassroomId())
                    )));
        }

        return scheduleEntries;
    }

    private Student resolveStudent(Long studentId) {
        if (studentId == null) {
            return null;
        }
        return studentRepository.findById(studentId).orElse(null);
    }

    private Course resolveCourse(CourseGroup courseGroup) {
        if (courseGroup == null) {
            return null;
        }

        Course course = courseGroup.getCourse();
        if (course != null) {
            return course;
        }

        Long courseId = courseGroup.getCourseId();
        if (courseId == null) {
            return null;
        }
        return courseRepository.findById(courseId).orElse(null);
    }

    private Course createCourseSummary(Course sourceCourse) {
        Course summary = new Course();
        summary.setCourseId(sourceCourse.getCourseId());
        summary.setCourseCode(sourceCourse.getCourseCode());
        summary.setName(sourceCourse.getName());
        summary.setCredits(sourceCourse.getCredits());
        summary.setSyllabusId(sourceCourse.getSyllabusId());
        summary.setLabHours(sourceCourse.getLabHours());
        summary.setPracticeHours(sourceCourse.getPracticeHours());
        summary.setTheoryHours(sourceCourse.getTheoryHours());
        summary.setSemesterNumber(sourceCourse.getSemesterNumber());
        summary.setGroups(new ArrayList<>());
        return summary;
    }

    private CourseGroup createCourseGroupSummary(CourseGroup courseGroup, Course parentCourse) {
        CourseGroup summaryGroup = new CourseGroup();
        summaryGroup.setId(courseGroup.getId());
        summaryGroup.setLetter(courseGroup.getLetter());
        summaryGroup.setType(courseGroup.getType());
        summaryGroup.setMaxCapacity(courseGroup.getMaxCapacity());
        summaryGroup.setAvailableCapacity(courseGroup.getAvailableCapacity());
        summaryGroup.setTeacherId(courseGroup.getTeacherId());
        summaryGroup.setCourseId(courseGroup.getCourseId());
        summaryGroup.setCourse(parentCourse);
        summaryGroup.setCourseSchedules(courseGroup.getCourseSchedules());
        return summaryGroup;
    }

    private String formatTime(java.time.LocalTime time) {
        return time != null ? time.toString() : "";
    }

    private String resolveClassroomName(Long classroomId) {
        if (classroomId == null) {
            return "";
        }
        return "Aula " + classroomId;
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
