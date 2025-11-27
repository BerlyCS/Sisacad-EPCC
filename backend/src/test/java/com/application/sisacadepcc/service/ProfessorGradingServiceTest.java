package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.presentation.dto.GradeSubmissionRequest;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.CourseGroupRepository;
import com.application.sisacadepcc.domain.repository.EnrollmentRepository;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessorGradingServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private GradeComputationService gradeComputationService;

    @Mock
    private SyllabusService syllabusService;

    private ProfessorGradingService professorGradingService;

    @BeforeEach
    void setUp() {
        professorGradingService = new ProfessorGradingService(
                courseRepository,
                courseGroupRepository,
                enrollmentRepository,
                gradeRepository,
                gradeComputationService,
                syllabusService
        );
    }

    @Test
    void submitGrade_requiresSyllabus() {
        Long courseId = 100L;
        Long groupId = 10L;
        Course course = new Course();
        course.setCourseId(courseId);

        CourseGroup group = new CourseGroup();
        group.setId(groupId);
        group.setCourse(course);
        group.setType(CourseType.THEORY);

        org.mockito.Mockito.lenient().when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        org.mockito.Mockito.lenient().when(courseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(syllabusService.getByCourseId(courseId)).thenReturn(Optional.empty());

        Professor professor = new Professor();
        professor.setUserId(1L);

        GradeSubmissionRequest request = new GradeSubmissionRequest(List.of(10), List.of(10), "SUBMITTED", null);

        assertThrows(IllegalArgumentException.class, () -> professorGradingService.submitGrade(courseId, groupId, 200L, request, professor));
    }
}
