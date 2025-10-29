package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.StudentCourse;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentCourseService {

    private final StudentCourseRepository studentCourseRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

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
                .collect(Collectors.toMap(Student::getDocumentoIdentidad, student -> student));

        // Filtrar estudiantes matriculados en el curso
        return enrollments.stream()
                .map(enrollment -> studentMap.get(enrollment.getStudentDocumentoIdentidad()))
                .filter(student -> student != null)
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesByStudent(String studentDocumentoIdentidad) {
        List<StudentCourse> enrollments = studentCourseRepository.findByStudentDocumentoIdentidad(studentDocumentoIdentidad);
        List<Course> allCourses = courseRepository.findAll();

        // Crear un mapa de cursos por ID para busqueda rapida
        java.util.Map<Long, Course> courseMap = allCourses.stream()
                .collect(Collectors.toMap(Course::getCourseID, course -> course));

        // Filtrar cursos en los que el estudiante está matriculado
        return enrollments.stream()
                .map(enrollment -> courseMap.get(enrollment.getCourseId()))
                .filter(course -> course != null)
                .collect(Collectors.toList());
    }

    @Transactional
    public void enrollStudentInCourse(String studentDocumentoIdentidad, Long courseId) {
        if (!studentCourseRepository.existsByStudentAndCourse(studentDocumentoIdentidad, courseId)) {
            StudentCourse enrollment = new StudentCourse(null, studentDocumentoIdentidad, courseId);
            studentCourseRepository.save(enrollment);
        }
    }

    public List<StudentCourse> getAllEnrollments() {
        return studentCourseRepository.findAll();
    }
}
