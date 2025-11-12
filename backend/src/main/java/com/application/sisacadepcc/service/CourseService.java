package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.StudentCourse;
import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;
import com.application.sisacadepcc.service.dto.CourseDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository repository;
    private final StudentCourseRepository studentCourseRepository;
    private final StudentRepository studentRepository;
    private final SyllabusRepository syllabusRepository;

    public CourseService(CourseRepository repository,
                         StudentCourseRepository studentCourseRepository,
                         StudentRepository studentRepository,
                         SyllabusRepository syllabusRepository) {
        this.repository = repository;
        this.studentCourseRepository = studentCourseRepository;
        this.studentRepository = studentRepository;
        this.syllabusRepository = syllabusRepository;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Optional<CourseDetails> getCourseDetails(Long courseId) {
        if (courseId == null) {
            return Optional.empty();
        }

        return repository.findById(courseId)
                .map(this::buildCourseDetails);
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
