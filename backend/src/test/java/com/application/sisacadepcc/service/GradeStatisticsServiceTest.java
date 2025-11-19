package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import com.application.sisacadepcc.presentation.dto.ProfessorCourseGradeStatsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeStatisticsServiceTest {

    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private CourseLookupService courseLookupService;

    private GradeStatisticsService gradeStatisticsService;

    @BeforeEach
    void setUp() {
        gradeStatisticsService = new GradeStatisticsService(
                gradeRepository,
                courseLookupService,
                new GradeComputationService()
        );
    }

    @Test
    void computesAggregateStatsForCourse() {
        Grade gradeOne = new Grade(
                1L,
                "STU-1",
                "101",
                1L,
                List.of(10, 12, 14),
                List.of(15, 18, 17)
        );
        Grade gradeTwo = new Grade(
                2L,
                "STU-2",
                "101",
                1L,
                List.of(14, 16, 18),
                List.of(12, 14, 16)
        );

        Course course = new Course();
        course.setCourseId(101L);
        course.setCourseType(CourseType.THEORY);
        course.setContinuousGradeWeights(List.of(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1)));
        course.setExamGradeWeights(List.of(BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.3)));

        when(gradeRepository.findByCourseCode("101")).thenReturn(List.of(gradeOne, gradeTwo));
        when(courseLookupService.findByCode("101")).thenReturn(Optional.of(course));

        ProfessorCourseGradeStatsResponse response = gradeStatisticsService.getStatisticsForCourse("101").orElseThrow();

        assertEquals(2, response.gradedStudents());
        assertTrue(response.meanFinalGrade() >= 15.0 && response.meanFinalGrade() <= 15.1);
        assertEquals(10.0, response.lowestGrade());
        assertEquals(18.0, response.highestGrade());
    }
}
