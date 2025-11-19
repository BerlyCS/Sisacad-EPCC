package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import com.application.sisacadepcc.presentation.dto.StudentGradeResponse;
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
class GradeQueryServiceTest {

    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private StudentCourseRepository studentCourseRepository;
    @Mock
    private CourseLookupService courseLookupService;

    private GradeQueryService gradeQueryService;

    @BeforeEach
    void setUp() {
        gradeQueryService = new GradeQueryService(
                gradeRepository,
                studentCourseRepository,
                courseLookupService,
                new GradeComputationService()
        );
    }

    @Test
    void returnsGradesOnlyForTheoryCoursesWhereStudentIsEnrolled() {
        Grade grade = new Grade(
                1L,
                "STU-1",
                "101",
                1L,
                List.of(10, 12, 14),
                List.of(16, 18, 19)
        );

        Course course = new Course();
        course.setCourseId(101L);
        course.setName("Programación");
        course.setCourseType(CourseType.THEORY);
        course.setContinuousGradeWeights(List.of(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1)));
        course.setExamGradeWeights(List.of(BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.3)));

        when(gradeRepository.findByStudentDocumento("STU-1")).thenReturn(List.of(grade));
        when(courseLookupService.findByCode("101")).thenReturn(Optional.of(course));
        when(studentCourseRepository.existsByStudentAndCourse("STU-1", 101L)).thenReturn(true);

        List<StudentGradeResponse> responses = gradeQueryService.getGradesForStudent("STU-1");

        assertEquals(1, responses.size());
        StudentGradeResponse response = responses.get(0);
        assertEquals("Programación", response.courseName());
        assertTrue(response.finalGrade().doubleValue() > 0);
    }
}
