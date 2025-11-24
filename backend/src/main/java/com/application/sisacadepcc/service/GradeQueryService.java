package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import com.application.sisacadepcc.presentation.dto.StudentGradeResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GradeQueryService {

    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;
    private final GradeComputationService gradeComputationService;

    public GradeQueryService(GradeRepository gradeRepository,
                             CourseRepository courseRepository,
                             GradeComputationService gradeComputationService) {
        this.gradeRepository = gradeRepository;
        this.courseRepository = courseRepository;
        this.gradeComputationService = gradeComputationService;
    }

    public List<StudentGradeResponse> getGradesForStudent(Long studentId) {
        return gradeRepository.findByStudent(studentId)
                .stream()
                .map(grade -> mapIfAllowed(grade, studentId))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public Optional<StudentGradeResponse> getGradeForCourse(Long studentId, String courseCode) {
        return gradeRepository.findByCourseAndStudent(courseCode, studentId)
                .flatMap(grade -> mapIfAllowed(grade, studentId));
    }

    private Optional<StudentGradeResponse> mapIfAllowed(Grade grade, Long studentId) {
        if (grade.getStudentId() == null || !grade.getStudentId().equals(studentId)) {
            return Optional.empty();
        }

        return courseRepository.findByCourseCode(Long.parseLong(grade.getCourseCode()))
                .filter(course -> course.getCourseId() != null)
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
