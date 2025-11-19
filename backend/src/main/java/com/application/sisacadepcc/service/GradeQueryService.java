package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import com.application.sisacadepcc.presentation.dto.StudentGradeResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GradeQueryService {

    private final GradeRepository gradeRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CourseLookupService courseLookupService;
    private final GradeComputationService gradeComputationService;

    public GradeQueryService(GradeRepository gradeRepository,
                             StudentCourseRepository studentCourseRepository,
                             CourseLookupService courseLookupService,
                             GradeComputationService gradeComputationService) {
        this.gradeRepository = gradeRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.courseLookupService = courseLookupService;
        this.gradeComputationService = gradeComputationService;
    }

    public List<StudentGradeResponse> getGradesForStudent(String studentDocumentoIdentidad) {
        return gradeRepository.findByStudentDocumento(studentDocumentoIdentidad)
                .stream()
                .map(grade -> mapIfAllowed(grade, studentDocumentoIdentidad))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public Optional<StudentGradeResponse> getGradeForCourse(String studentDocumentoIdentidad, String courseCode) {
        return gradeRepository.findByCourseAndStudent(courseCode, studentDocumentoIdentidad)
                .flatMap(grade -> mapIfAllowed(grade, studentDocumentoIdentidad));
    }

    private Optional<StudentGradeResponse> mapIfAllowed(Grade grade, String studentDocumentoIdentidad) {
        if (!studentDocumentoIdentidad.equalsIgnoreCase(grade.getStudentDocumentoIdentidad())) {
            return Optional.empty();
        }

        return courseLookupService.findByCode(grade.getCourseCode())
                .filter(course -> course.getCourseType() == CourseType.THEORY)
                .filter(course -> course.getCourseId() != null)
                .filter(course -> studentCourseRepository.existsByStudentAndCourse(studentDocumentoIdentidad, course.getCourseId()))
                .map(course -> toDto(grade, course));
    }

    private StudentGradeResponse toDto(Grade grade, Course course) {
        GradeComputationService.GradeWeightSnapshot weights = gradeComputationService.snapshotWeights(course);
        return new StudentGradeResponse(
                grade.getCourseCode(),
                course.getCourseId(),
                course.getName(),
                grade.getContinuousGrades(),
                grade.getExamGrades(),
                weights.continuousWeights(),
                weights.examWeights(),
                gradeComputationService.computeFinalGrade(grade, course)
        );
    }
}
