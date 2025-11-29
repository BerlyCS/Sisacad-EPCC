package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Grade;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeStatisticsServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private CourseLookupService courseLookupService;

    @Mock
    private GradeComputationService gradeComputationService;

    private GradeStatisticsService service;

    @BeforeEach
    void setUp() {
        service = new GradeStatisticsService(gradeRepository, courseLookupService, gradeComputationService);
    }

    @Test
    void getStatisticsForCourse_returnsEmptyWhenNoGrades() {
        Long courseId = 5L;
        when(gradeRepository.findByCourseId(courseId)).thenReturn(List.of());

        Optional<ProfessorCourseGradeStatsResponse> result = service.getStatisticsForCourse(courseId);

        assertTrue(result.isEmpty());
        verify(gradeRepository).findByCourseId(courseId);
        verifyNoInteractions(courseLookupService, gradeComputationService);
    }

    @Test
    void getStatisticsForCourse_computesAggregates() {
        Long courseId = 10L;
        Grade gradeOne = new Grade(1L, 101L, courseId, 201L,
                List.of(10, 15, 18), List.of(18));
        Grade gradeTwo = new Grade(2L, 102L, courseId, 202L,
                List.of(12, 16, 20), List.of(19, 17));
        List<Grade> grades = List.of(gradeOne, gradeTwo);
        when(gradeRepository.findByCourseId(courseId)).thenReturn(grades);

        Course course = new Course();
        course.setCourseCode(3210);
        when(courseLookupService.findById(courseId)).thenReturn(Optional.of(course));

        when(gradeComputationService.computeFinalGrade(eq(gradeOne), eq(course)))
                .thenReturn(BigDecimal.valueOf(15));
        when(gradeComputationService.computeFinalGrade(eq(gradeTwo), eq(course)))
                .thenReturn(BigDecimal.valueOf(17));

        Optional<ProfessorCourseGradeStatsResponse> result = service.getStatisticsForCourse(courseId);

        assertTrue(result.isPresent());
        ProfessorCourseGradeStatsResponse stats = result.get();
        assertEquals("3210", stats.courseCode());

        double expectedMean = Stream.of(
                gradeOne.getContinuousGrades(),
                gradeOne.getExamGrades(),
                gradeTwo.getContinuousGrades(),
                gradeTwo.getExamGrades())
            .flatMap(List::stream)
            .filter(java.util.Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0);
        int expectedMax = Stream.of(
                gradeOne.getContinuousGrades(),
                gradeOne.getExamGrades(),
                gradeTwo.getContinuousGrades(),
                gradeTwo.getExamGrades())
            .flatMap(List::stream)
            .filter(java.util.Objects::nonNull)
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0);
        int expectedMin = Stream.of(
                gradeOne.getContinuousGrades(),
                gradeOne.getExamGrades(),
                gradeTwo.getContinuousGrades(),
                gradeTwo.getExamGrades())
            .flatMap(List::stream)
            .filter(java.util.Objects::nonNull)
            .mapToInt(Integer::intValue)
            .min()
            .orElse(0);

        assertNotNull(stats.meanGrade());
        assertEquals(expectedMean, stats.meanGrade(), 0.0001);
        assertEquals((double) expectedMax, stats.highestGrade());
        assertEquals((double) expectedMin, stats.lowestGrade());
        assertEquals(16.0, stats.meanFinalGrade());
        assertEquals(17.0, stats.highestFinalGrade());
        assertEquals(15.0, stats.lowestFinalGrade());
        assertEquals(2, stats.gradedStudents());

        verify(gradeRepository).findByCourseId(courseId);
    }
}
