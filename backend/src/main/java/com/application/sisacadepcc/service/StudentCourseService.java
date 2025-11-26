package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.domain.repository.CourseGroupRepository;
import com.application.sisacadepcc.domain.repository.EnrollmentRepository;
import com.application.sisacadepcc.infrastructure.repository.jpa.EnrollmentEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.StudentJpaRepository;
import com.application.sisacadepcc.infrastructure.repository.jpa.CourseGroupJpaRepository;
import com.application.sisacadepcc.presentation.dto.StudentScheduleEntry;
import com.application.sisacadepcc.service.dto.EnrollmentValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        List<Course> summaries = new ArrayList<>();
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

            summaries.add(buildCourseSummary(course, courseGroup));
        }

        return summaries;
    }

    @Transactional
    public void enrollStudentInCourse(Long studentId, Long courseId) {
        // Enrollment functionality removed
    }

    @Transactional
    public void enrollStudentInCourseGroup(Long studentId, Long courseGroupId) {
        if (studentId == null || courseGroupId == null) {
            throw new IllegalArgumentException("Student ID and Course Group ID are required");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Course group not found"));

        // Check if already enrolled
        boolean alreadyEnrolled = enrollmentRepository.findByCourseGroupId(courseGroupId).stream()
                .anyMatch(enrollment -> enrollment.getStudent().getCui().equals(student.getCui()));

        if (alreadyEnrolled) {
            throw new IllegalStateException("Student is already enrolled in this course group");
        }

        // Check capacity
        long currentEnrollments = enrollmentRepository.countByCourseGroupId(courseGroupId);
        if (currentEnrollments >= courseGroup.getMaxCapacity()) {
            throw new IllegalStateException("Course group is at full capacity");
        }

        // Get entities for enrollment
        var studentEntity = studentJpaRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student entity not found"));
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

    private Course buildCourseSummary(Course sourceCourse, CourseGroup courseGroup) {
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

        CourseGroup summaryGroup = new CourseGroup();
        summaryGroup.setId(courseGroup.getId());
        summaryGroup.setLetter(courseGroup.getLetter());
        summaryGroup.setType(courseGroup.getType());
        summaryGroup.setMaxCapacity(courseGroup.getMaxCapacity());
        summaryGroup.setAvailableCapacity(courseGroup.getAvailableCapacity());
        summaryGroup.setTeacherId(courseGroup.getTeacherId());
        summaryGroup.setCourseId(courseGroup.getCourseId());
        summaryGroup.setCourse(summary);
        summaryGroup.setCourseSchedules(courseGroup.getCourseSchedules());

        summary.setGroups(List.of(summaryGroup));
        return summary;
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
