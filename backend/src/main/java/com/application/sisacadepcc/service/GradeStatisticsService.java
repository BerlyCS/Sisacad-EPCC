package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import com.application.sisacadepcc.presentation.dto.ProfessorCourseGradeStatsResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class GradeStatisticsService {

    private final GradeRepository gradeRepository;
    private final CourseLookupService courseLookupService;
    private final GradeComputationService gradeComputationService;

    public GradeStatisticsService(GradeRepository gradeRepository,
                                  CourseLookupService courseLookupService,
                                  GradeComputationService gradeComputationService) {
        this.gradeRepository = gradeRepository;
        this.courseLookupService = courseLookupService;
        this.gradeComputationService = gradeComputationService;
    }

    public Optional<ProfessorCourseGradeStatsResponse> getStatisticsForCourse(Long courseId) {
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        if (grades.isEmpty()) {
            return Optional.empty();
        }

        IntSummaryStatistics mergedStats = grades.stream()
            .flatMap(grade -> Stream.concat(
                grade.getContinuousGrades().stream(),
                grade.getExamGrades().stream()))
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .summaryStatistics();

        Course course = courseLookupService.findById(courseId).orElse(null);
        DoubleSummaryStatistics finalStats = grades.stream()
                .map(grade -> gradeComputationService.computeFinalGrade(grade, course))
                .mapToDouble(BigDecimal::doubleValue)
                .summaryStatistics();

        ProfessorCourseGradeStatsResponse response = new ProfessorCourseGradeStatsResponse(
            course != null ? String.valueOf(course.getCourseCode()) : null,
                mergedStats.getCount() > 0 ? mergedStats.getAverage() : null,
                mergedStats.getCount() > 0 ? (double) mergedStats.getMax() : null,
                mergedStats.getCount() > 0 ? (double) mergedStats.getMin() : null,
                finalStats.getCount() > 0 ? finalStats.getAverage() : null,
                finalStats.getCount() > 0 ? finalStats.getMax() : null,
                finalStats.getCount() > 0 ? finalStats.getMin() : null,
                (int) finalStats.getCount()
        );

        return Optional.of(response);
    }

}
