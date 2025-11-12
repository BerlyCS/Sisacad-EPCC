package com.application.sisacadepcc.service.dto;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Syllabus;
import java.util.List;

public record CourseDetails(
        Course course,
        List<Student> enrolledStudents,
        Course associatedLabCourse,
        Syllabus syllabus
) {
}
