package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseGroupRepository;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.EnrollmentRepository;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import com.application.sisacadepcc.presentation.dto.CourseGroupSummaryResponse;
import com.application.sisacadepcc.presentation.dto.CourseRosterEntryResponse;
import com.application.sisacadepcc.presentation.dto.CourseRosterPageResponse;
import com.application.sisacadepcc.presentation.dto.GradeSubmissionRequest;
import com.application.sisacadepcc.presentation.dto.GradeSubmissionResponse;
import com.application.sisacadepcc.presentation.dto.GradingRubricResponse;
import com.application.sisacadepcc.infrastructure.repository.jpa.EnrollmentEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.StudentEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProfessorGradingService {

    private static final int MAX_GROUP_CAPACITY = 150;
    private static final int MAX_COMPONENTS = 3;

    private final CourseRepository courseRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final GradeComputationService gradeComputationService;
    private final SyllabusService syllabusService;

    public ProfessorGradingService(CourseRepository courseRepository,
                                   CourseGroupRepository courseGroupRepository,
                                   EnrollmentRepository enrollmentRepository,
                                   GradeRepository gradeRepository,
                                   GradeComputationService gradeComputationService,
                                   SyllabusService syllabusService) {
        this.courseRepository = courseRepository;
        this.courseGroupRepository = courseGroupRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.gradeRepository = gradeRepository;
        this.gradeComputationService = gradeComputationService;
        this.syllabusService = syllabusService;
    }

    public List<CourseGroupSummaryResponse> getCourseGroups(Long courseId, Professor professor, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el curso solicitado"));

        List<CourseGroup> courseGroups = courseGroupRepository.findByCourseId(courseId);
        ensureCourseAccess(courseGroups, professor, isAdmin);

        return courseGroups.stream()
                .map(group -> toGroupSummary(course, group, canGradeGroup(group, professor, isAdmin)))
                .sorted(Comparator.comparing(CourseGroupSummaryResponse::groupLetter, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public CourseRosterPageResponse getCourseRoster(Long courseId,
                                                    List<Long> targetGroupIds,
                                                    int page,
                                                    int size,
                                                    Professor professor,
                                                    boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el curso solicitado"));

        List<CourseGroup> allGroups = courseGroupRepository.findByCourseId(courseId);
        ensureCourseAccess(allGroups, professor, isAdmin);

        List<CourseGroup> resolvedGroups = resolveTargetGroups(allGroups, targetGroupIds);
        Map<Long, Grade> gradesByStudent = indexGradesByStudent(courseId);
        List<CourseRosterEntryResponse> entries = new ArrayList<>();

        for (CourseGroup group : resolvedGroups) {
            List<EnrollmentEntity> enrolments = enrollmentRepository.findByCourseGroupId(group.getId());
            if (enrolments.isEmpty()) {
                continue;
            }

            boolean groupCanGrade = canGradeGroup(group, professor, isAdmin);
            for (EnrollmentEntity enrolment : enrolments) {
                StudentEntity student = enrolment.getStudent();
                if (student == null || student.getUserId() == null) {
                    continue;
                }

                Grade grade = gradesByStudent.get(student.getUserId());
                entries.add(toRosterEntry(course, group, student, grade, groupCanGrade));
            }
        }

        entries.sort(Comparator.comparing(CourseRosterEntryResponse::fullName,
                Comparator.nullsLast(String::compareToIgnoreCase)));

        int safeSize = Math.min(Math.max(size, 1), MAX_GROUP_CAPACITY);
        int safePage = Math.max(page, 0);
        int total = entries.size();
        int fromIndex = Math.min(safePage * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);
        List<CourseRosterEntryResponse> pageContent = entries.subList(fromIndex, toIndex);

        return new CourseRosterPageResponse(pageContent, total, safePage, safeSize);
    }

    public GradingRubricResponse getCourseRubric(Long courseId, Professor professor, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el curso solicitado"));
        List<CourseGroup> courseGroups = courseGroupRepository.findByCourseId(courseId);
        ensureCourseAccess(courseGroups, professor, isAdmin);

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
                                               Long studentId,
                                               GradeSubmissionRequest request,
                                               Professor professor) {
        if (professor == null) {
            throw new AccessDeniedException("Solo los profesores autorizados pueden registrar notas");
        }

        // Enforce syllabus presence: professor cannot submit grades if no syllabus uploaded
        syllabusService.getByCourseId(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Syllabus not found for this course. Please upload a syllabus first."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el curso seleccionado"));

        CourseGroup group = courseGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el grupo especificado"));

        if (!Objects.equals(group.getCourseId(), courseId)) {
            throw new IllegalArgumentException("El grupo no pertenece al curso indicado");
        }

        if (!canGradeGroup(group, professor, false)) {
            throw new AccessDeniedException("No tienes permiso para registrar notas en este grupo");
        }

        EnrollmentEntity enrollment = enrollmentRepository.findByCourseGroupId(groupId).stream()
                .filter(e -> e.getStudent() != null && Objects.equals(e.getStudent().getUserId(), studentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El estudiante no pertenece al grupo indicado"));

        if (enrollment.getStudent() == null) {
            throw new IllegalArgumentException("No se pudo resolver la información del estudiante");
        }

        List<Integer> sanitizedContinuous = sanitizeGrades(request.continuousGrades());
        List<Integer> sanitizedExam = sanitizeGrades(request.examGrades());

        Grade persisted = gradeRepository.findByCourseAndStudent(courseId, studentId).orElse(null);
        Grade grade = new Grade(
                persisted != null ? persisted.getGradeID() : null,
                studentId,
                courseId,
                resolveProfessorId(professor),
                sanitizedContinuous,
                sanitizedExam
        );

        gradeRepository.save(grade);

        BigDecimal finalGrade = gradeComputationService.computeFinalGrade(grade, course);
        return new GradeSubmissionResponse(
                groupId,
                courseId,
            resolveCourseCode(course),
                studentId,
                sanitizedContinuous,
                sanitizedExam,
                finalGrade.doubleValue(),
                Optional.ofNullable(request.status()).orElse("SUBMITTED")
        );
    }

    private void ensureCourseAccess(List<CourseGroup> courseGroups,
                                    Professor professor,
                                    boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (CollectionUtils.isEmpty(courseGroups)) {
            throw new AccessDeniedException("No se encontraron grupos asociados al curso");
        }

        Long professorId = resolveProfessorId(professor);
        boolean assigned = courseGroups.stream()
                .map(CourseGroup::getTeacherId)
                .filter(Objects::nonNull)
                .anyMatch(id -> Objects.equals(id, professorId));

        if (!assigned) {
            throw new AccessDeniedException("No tienes acceso a este curso");
        }
    }

    private List<CourseGroup> resolveTargetGroups(List<CourseGroup> courseGroups, List<Long> requestedGroupIds) {
        if (courseGroups == null) {
            return List.of();
        }
        if (CollectionUtils.isEmpty(requestedGroupIds)) {
            return courseGroups;
        }

        Map<Long, CourseGroup> groupsById = new HashMap<>();
        for (CourseGroup group : courseGroups) {
            if (group != null && group.getId() != null) {
                groupsById.put(group.getId(), group);
            }
        }

        List<CourseGroup> resolved = new ArrayList<>();
        for (Long groupId : requestedGroupIds) {
            if (groupId == null) {
                continue;
            }
            CourseGroup group = groupsById.get(groupId);
            if (group != null) {
                resolved.add(group);
            }
        }
        return resolved.isEmpty() ? courseGroups : resolved;
    }

    private CourseGroupSummaryResponse toGroupSummary(Course course, CourseGroup group, boolean canGrade) {
        long enrollmentCount = group.getId() != null
                ? enrollmentRepository.countByCourseGroupId(group.getId())
                : 0;

        int studentCount = enrollmentCount > Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : (int) enrollmentCount;

        int capacity = group.getMaxCapacity() > 0 ? group.getMaxCapacity() : MAX_GROUP_CAPACITY;

        return new CourseGroupSummaryResponse(
            group.getId(),
            course.getCourseId(),
            resolveCourseCode(course),
                course.getName(),
                group.getLetter() != null ? group.getLetter() : "-",
                resolveCourseType(group),
                canGrade,
                studentCount,
                capacity
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

    private boolean isGradableGroup(CourseGroup group) {
        if (group == null || group.getType() == null) {
            return false;
        }
        return group.getType() == CourseType.THEORY || group.getType() == CourseType.PRACTICE;
    }

    private boolean canGradeGroup(CourseGroup group, Professor professor, boolean isAdmin) {
        if (group == null) {
            return false;
        }
        if (!isGradableGroup(group)) {
            return false;
        }
        if (isAdmin) {
            return true;
        }
        Long professorId = resolveProfessorId(professor);
        return Objects.equals(group.getTeacherId(), professorId);
    }

    private Map<Long, Grade> indexGradesByStudent(Long courseId) {
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        Map<Long, Grade> indexed = new HashMap<>();
        for (Grade grade : grades) {
            if (grade.getStudentId() != null) {
                indexed.put(grade.getStudentId(), grade);
            }
        }
        return indexed;
    }

    private CourseRosterEntryResponse toRosterEntry(Course course,
                                                    CourseGroup group,
                                                    StudentEntity student,
                                                    Grade grade,
                                                    boolean canGradeGroup) {
        List<Integer> continuousGrades = grade != null ? grade.getContinuousGrades() : Collections.emptyList();
        List<Integer> examGrades = grade != null ? grade.getExamGrades() : Collections.emptyList();

        Double finalGrade = null;
        if (grade != null) {
            finalGrade = gradeComputationService.computeFinalGrade(grade, course).doubleValue();
        }

        return new CourseRosterEntryResponse(
                student.getUserId(),
                student.getCui(),
                formatStudentName(student),
                student.getInstitutionalEmail(),
                group.getId(),
                course.getCourseId(),
            resolveCourseCode(course),
                group.getLetter() != null ? group.getLetter() : "-",
                resolveCourseType(group),
                canGradeGroup,
                continuousGrades,
                examGrades,
                finalGrade,
                grade != null ? "SUBMITTED" : "PENDING"
        );
    }

    private String resolveCourseType(CourseGroup group) {
        CourseType type = group != null ? group.getType() : null;
        return type != null ? type.name() : CourseType.THEORY.name();
    }

    private String resolveCourseCode(Course course) {
        if (course == null) {
            return "";
        }
        Integer courseCode = course.getCourseCode();
        if (courseCode != null) {
            return courseCode.toString();
        }
        return course.getCourseId() != null ? course.getCourseId().toString() : "";
    }

    private String formatStudentName(StudentEntity student) {
        StringBuilder builder = new StringBuilder();
        if (student.getPaternalSurname() != null) {
            builder.append(student.getPaternalSurname()).append(' ');
        }
        if (student.getMaternalSurname() != null) {
            builder.append(student.getMaternalSurname()).append(' ');
        }
        if (student.getFirstNames() != null) {
            builder.append(student.getFirstNames());
        }
        String fullName = builder.toString().trim();
        return fullName.isEmpty() ? student.getInstitutionalEmail() : fullName;
    }

    private Long resolveProfessorId(Professor professor) {
        if (professor == null) {
            throw new AccessDeniedException("No se pudo determinar el profesor autenticado");
        }
        return professor.getUserId();
    }
}
