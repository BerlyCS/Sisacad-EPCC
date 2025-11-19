package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.StudentCourse;
import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;
import com.application.sisacadepcc.service.dto.CourseDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository repository;
    private final StudentCourseRepository studentCourseRepository;
    private final StudentRepository studentRepository;
    private final SyllabusRepository syllabusRepository;
    private final ProfessorRepository professorRepository;

    public CourseService(CourseRepository repository,
                         StudentCourseRepository studentCourseRepository,
                         StudentRepository studentRepository,
                         SyllabusRepository syllabusRepository,
                         ProfessorRepository professorRepository) {
        this.repository = repository;
        this.studentCourseRepository = studentCourseRepository;
        this.studentRepository = studentRepository;
        this.syllabusRepository = syllabusRepository;
        this.professorRepository = professorRepository;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public List<Course> getCoursesForProfessor(Long professorId) {
        if (professorId == null) {
            return List.of();
        }

        return repository.findAll().stream()
                .filter(course -> course.getTeacherIDs() != null)
                .filter(course -> course.getTeacherIDs().stream()
                        .filter(Objects::nonNull)
                        .anyMatch(id -> Objects.equals(id, professorId)))
                .collect(Collectors.toList());
    }

    public Optional<CourseDetails> getCourseDetails(Long courseId) {
        if (courseId == null) {
            return Optional.empty();
        }

        return repository.findById(courseId)
                .map(this::buildCourseDetails);
    }

    public Optional<Course> assignProfessorToCourse(Long courseId, Long professorId) {
        if (courseId == null || professorId == null) {
            return Optional.empty();
        }

        if (professorRepository.findById(professorId).isEmpty()) {
            return Optional.empty();
        }

        return repository.findById(courseId)
                .flatMap(course -> {
                    List<Long> teacherIds = new ArrayList<>(
                            Optional.ofNullable(course.getTeacherIDs()).orElse(List.of())
                    );
                    if (teacherIds.contains(professorId)) {
                        return Optional.of(course);
                    }
                    teacherIds.add(professorId);
                    return repository.updateTeacherAssignments(courseId, teacherIds);
                });
    }

    public Optional<Course> removeProfessorFromCourse(Long courseId, Long professorId) {
        if (courseId == null || professorId == null) {
            return Optional.empty();
        }

        return repository.findById(courseId)
                .flatMap(course -> {
                    List<Long> teacherIds = new ArrayList<>(
                            Optional.ofNullable(course.getTeacherIDs()).orElse(List.of())
                    );
                    boolean modified = teacherIds.removeIf(id -> Objects.equals(id, professorId));
                    if (!modified) {
                        return Optional.of(course);
                    }
                    return repository.updateTeacherAssignments(courseId, teacherIds);
                });
    }

    private CourseDetails buildCourseDetails(Course course) {
        List<StudentCourse> enrolments = studentCourseRepository.findByCourseId(course.getCourseId());
        List<String> studentIds = enrolments.stream()
                .map(StudentCourse::getStudentDocumentoIdentidad)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Student> students = studentRepository.findByDocumentoIdentidadIn(studentIds);

        Course labCourse = null;
        if (course.getCourseType() == CourseType.THEORY && course.getLabPrerequisiteCourseId() != null) {
            labCourse = repository.findById(course.getLabPrerequisiteCourseId()).orElse(null);
        }

        Syllabus syllabus = null;
        if (course.getSyllabusID() != null) {
            syllabus = syllabusRepository.findById(course.getSyllabusID()).orElse(null);
        }

        return new CourseDetails(course, students, labCourse, syllabus);
    }

}
