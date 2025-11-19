package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GradeComputationServiceTest {

    private GradeComputationService computationService;
    private Course course;

    @BeforeEach
    void setUp() {
        computationService = new GradeComputationService();
        course = new Course();
        course.setCourseId(1L);
        course.setCourseType(CourseType.THEORY);
        course.setContinuousGradeWeights(List.of(
                BigDecimal.valueOf(0.1),
                BigDecimal.valueOf(0.1),
                BigDecimal.valueOf(0.1)
        ));
        course.setExamGradeWeights(List.of(
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3)
        ));
    }

    @Test
    void computesFinalGradeUsingProvidedWeights() {
        Grade grade = new Grade(
                1L,
                "STU-1",
                "COURSE-1",
                10L,
                List.of(10, 12, 14),
                List.of(15, 18, 17)
        );

        BigDecimal finalGrade = computationService.computeFinalGrade(grade, course);
        assertEquals(new BigDecimal("15.30"), finalGrade);
    }

    @Test
    void fallsBackToEqualWeightsWhenMissing() {
        Course courseWithoutWeights = new Course();
        courseWithoutWeights.setCourseId(2L);
        courseWithoutWeights.setCourseType(CourseType.THEORY);

        Grade grade = new Grade(
                2L,
                "STU-2",
                "COURSE-2",
                11L,
                List.of(10, 10, 10),
                List.of(20, 20, 20)
        );

        BigDecimal finalGrade = computationService.computeFinalGrade(grade, courseWithoutWeights);
        assertEquals(new BigDecimal("15.00"), finalGrade);
    }
}
