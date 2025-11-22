package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.StudentCourse;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.presentation.dto.CourseGroupSummaryResponse;
import com.application.sisacadepcc.presentation.dto.CourseRosterEntryResponse;
import com.application.sisacadepcc.presentation.dto.CourseRosterPageResponse;
import com.application.sisacadepcc.presentation.dto.GradeSubmissionRequest;
import com.application.sisacadepcc.presentation.dto.GradeSubmissionResponse;
import com.application.sisacadepcc.presentation.dto.GradingRubricResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfessorGradingService {

    private static final int MAX_GROUP_CAPACITY = 150;
    private static final int MAX_COMPONENTS = 3;

    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final AuthorizationService authorizationService;
    private final GradeComputationService gradeComputationService;

    public ProfessorGradingService(CourseRepository courseRepository,
                                   StudentCourseRepository studentCourseRepository,
                                   StudentRepository studentRepository,
                                   GradeRepository gradeRepository,
                                   AuthorizationService authorizationService,
                                   GradeComputationService gradeComputationService) {
        this.courseRepository = courseRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
        this.authorizationService = authorizationService;
        this.gradeComputationService = gradeComputationService;
    }

    public List<CourseGroupSummaryResponse> getCourseGroups(Long courseId, Professor professor, boolean isAdmin) {
        Course anchorCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el curso solicitado"));

        ensureCourseAccess(anchorCourse, professor, isAdmin);

        List<Course> siblingGroups = resolveSiblingGroups(anchorCourse);
        return siblingGroups.stream()
                .map(course -> toGroupSummary(course, isTheory(course)))
                .toList();
    }

    public CourseRosterPageResponse getCourseRoster(Long courseId,
                                                    List<Long> targetGroupIds,
                                                    int page,
                                                    int size,
                                                    Professor professor,
                                                    boolean isAdmin) {
        Course anchorCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el curso solicitado"));

        ensureCourseAccess(anchorCourse, professor, isAdmin);

        List<Course> resolvedGroups = resolveTargetGroups(anchorCourse, targetGroupIds);
        List<CourseRosterEntryResponse> entries = new ArrayList<>();

        for (Course group : resolvedGroups) {
            List<StudentCourse> enrolments = studentCourseRepository.findByCourseId(group.getCourseId());
            if (enrolments.isEmpty()) {
                continue;
            }

            Map<String, Student> studentsByDocumento = fetchStudents(enrolments);
            boolean canGrade = isTheory(group);

            for (StudentCourse enrolment : enrolments) {
                Student student = studentsByDocumento.get(enrolment.getStudentDocumentoIdentidad());
                if (student == null) {
                    continue;
                }
                Grade grade = gradeRepository
                        .findByCourseAndStudent(String.valueOf(group.getCourseId()), student.getDocumentoIdentidad())
                        .orElse(null);

                Double finalGrade = null;
                if (grade != null && canGrade) {
                    BigDecimal computed = gradeComputationService.computeFinalGrade(grade, group);
                    finalGrade = computed.doubleValue();
                }

                String status = grade != null ? "SUBMITTED" : "PENDING";
                entries.add(toRosterEntry(student, group, grade, finalGrade, canGrade, status));
            }
        }

        entries.sort(Comparator.comparing(CourseRosterEntryResponse::fullName,
                Comparator.nullsLast(String::compareToIgnoreCase)));

        int safeSize = Math.min(Math.max(size, 1), MAX_GROUP_CAPACITY);
        int total = entries.size();
        int fromIndex = Math.min(Math.max(page, 0) * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);
        List<CourseRosterEntryResponse> pageContent = entries.subList(fromIndex, toIndex);

        return new CourseRosterPageResponse(pageContent, total, Math.max(page, 0), safeSize);
    }

    public GradingRubricResponse getCourseRubric(Long courseId, Professor professor, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el curso solicitado"));
        ensureCourseAccess(course, professor, isAdmin);

        GradeComputationService.GradeWeightSnapshot weights = gradeComputationService.snapshotWeights(course);
        return new GradingRubricResponse(
                course.getCourseId(),
                String.valueOf(course.getCourseId()),
                weights.continuousWeights(),
                weights.examWeights()
        );
    }

    public GradeSubmissionResponse submitGrade(Long courseId,
                                               Long groupId,
                                               String studentDocumento,
                                               GradeSubmissionRequest request,
                                               Professor professor) {
        if (professor == null) {
            throw new AccessDeniedException("Solo los profesores autorizados pueden registrar notas");
        }

        Course groupCourse = courseRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el grupo especificado"));

        ensureCourseAccess(groupCourse, professor, false);

        if (!Objects.equals(courseId, groupId)) {
            Course anchor = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el curso seleccionado"));
            ensureCourseAccess(anchor, professor, false);
            boolean belongsToCourse = resolveSiblingGroups(anchor).stream()
                .anyMatch(course -> Objects.equals(course.getCourseId(), groupId));
            if (!belongsToCourse) {
            throw new IllegalArgumentException("El grupo no pertenece al curso indicado");
            }
        }

        if (!isTheory(groupCourse)) {
            throw new AccessDeniedException("Los grupos de laboratorio son de solo lectura para notas");
        }

        if (!studentCourseRepository.existsByStudentAndCourse(studentDocumento, groupCourse.getCourseId())) {
            throw new IllegalArgumentException("El estudiante no está matriculado en este grupo");
        }

        List<Integer> sanitizedContinuous = sanitizeGrades(request.continuousGrades());
        List<Integer> sanitizedExam = sanitizeGrades(request.examGrades());

        Grade persisted = gradeRepository
                .findByCourseAndStudent(String.valueOf(groupCourse.getCourseId()), studentDocumento)
                .orElse(null);

        Grade grade = new Grade(
                persisted != null ? persisted.getGradeID() : null,
                studentDocumento,
                String.valueOf(groupCourse.getCourseId()),
                resolveProfessorId(professor),
                sanitizedContinuous,
                sanitizedExam
        );

        gradeRepository.save(grade);

        BigDecimal finalGrade = gradeComputationService.computeFinalGrade(grade, groupCourse);
        return new GradeSubmissionResponse(
                groupCourse.getCourseId(),
                String.valueOf(groupCourse.getCourseId()),
                studentDocumento,
                sanitizedContinuous,
                sanitizedExam,
                finalGrade.doubleValue(),
                Optional.ofNullable(request.status()).orElse("SUBMITTED")
        );
    }

    private void ensureCourseAccess(Course course, Professor professor, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        Long professorId = resolveProfessorId(professor);
        List<Long> teacherIds = Optional.ofNullable(course.getTeacherIDs()).orElse(List.of());
        boolean assigned = teacherIds.stream().filter(Objects::nonNull).anyMatch(id -> Objects.equals(id, professorId));
        if (!assigned) {
            throw new AccessDeniedException("No tienes acceso a este curso");
        }
    }

    private List<Course> resolveSiblingGroups(Course anchorCourse) {
        Long anchorCode = anchorCourse.getCourseCode() != null ? anchorCourse.getCourseCode() : anchorCourse.getCourseId();
        return courseRepository.findAll().stream()
                .filter(course -> Objects.equals(anchorCode, course.getCourseCode() != null ? course.getCourseCode() : course.getCourseId()))
                .sorted(Comparator.comparing(Course::getGroupLetter))
                .toList();
    }

    private List<Course> resolveTargetGroups(Course anchorCourse, List<Long> requestedGroupIds) {
        if (CollectionUtils.isEmpty(requestedGroupIds)) {
            return List.of(anchorCourse);
        }

        Map<Long, Course> courseById = resolveSiblingGroups(anchorCourse).stream()
                .collect(Collectors.toMap(Course::getCourseId, course -> course, (left, right) -> left, LinkedHashMap::new));

        List<Course> resolved = new ArrayList<>();
        for (Long groupId : requestedGroupIds) {
            Course course = courseById.get(groupId);
            if (course != null) {
                resolved.add(course);
            }
        }
        return resolved.isEmpty() ? List.of(anchorCourse) : resolved;
    }

    private Map<String, Student> fetchStudents(List<StudentCourse> enrolments) {
        Set<String> documentos = enrolments.stream()
                .map(StudentCourse::getStudentDocumentoIdentidad)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Student> students = studentRepository.findByDocumentoIdentidadIn(new ArrayList<>(documentos));
        Map<String, Student> byDocumento = new HashMap<>();
        for (Student student : students) {
            byDocumento.put(student.getDocumentoIdentidad(), student);
        }
        return byDocumento;
    }

    private CourseGroupSummaryResponse toGroupSummary(Course course, boolean canGrade) {
        int studentCount = studentCourseRepository.findByCourseId(course.getCourseId()).size();
        return new CourseGroupSummaryResponse(
                course.getCourseId(),
                String.valueOf(course.getCourseId()),
                course.getName(),
                String.valueOf(course.getGroupLetter()),
                course.getCourseType().name(),
                canGrade,
                studentCount,
                MAX_GROUP_CAPACITY
        );
    }

        private CourseRosterEntryResponse toRosterEntry(Student student,
                                Course group,
                                Grade grade,
                                Double finalGrade,
                                boolean canGrade,
                                String status) {
        String fullName = String.format(Locale.ROOT, "%s %s %s",
                Optional.ofNullable(student.getApellidoPaterno()).orElse(""),
                Optional.ofNullable(student.getApellidoMaterno()).orElse(""),
                Optional.ofNullable(student.getNombres()).orElse("")
        ).trim();

        return new CourseRosterEntryResponse(
                student.getDocumentoIdentidad(),
                student.getCui(),
                fullName.isEmpty() ? student.getNombres() : fullName,
                student.getCorreoInstitucional(),
                group.getCourseId(),
                String.valueOf(group.getCourseId()),
                String.valueOf(group.getGroupLetter()),
                group.getCourseType().name(),
                canGrade,
            grade != null ? grade.getContinuousGrades() : List.of(),
            grade != null ? grade.getExamGrades() : List.of(),
                finalGrade,
                status
        );
    }

    private List<Integer> sanitizeGrades(List<Integer> grades) {
        if (grades == null) {
            return List.of();
        }
        List<Integer> sanitized = new ArrayList<>();
        for (Integer grade : grades) {
            if (grade == null) {
                continue;
            }
            if (grade < 0 || grade > 20) {
                throw new IllegalArgumentException("Las notas deben estar entre 0 y 20");
            }
            sanitized.add(grade);
            if (sanitized.size() == MAX_COMPONENTS) {
                break;
            }
        }
        return sanitized;
    }

    private boolean isTheory(Course course) {
        return course.getCourseType() == null || course.getCourseType() == CourseType.THEORY;
    }

    private Long resolveProfessorId(Professor professor) {
        if (professor == null) {
            throw new AccessDeniedException("No se pudo determinar el profesor autenticado");
        }
        return professor.getId();
    }
}
