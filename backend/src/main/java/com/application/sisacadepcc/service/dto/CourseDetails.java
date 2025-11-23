package com.application.sisacadepcc.service.dto;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Syllabus;
import java.util.List;

public record CourseDetails(
        CourseGroup courseGroup,
        List<Student> enrolledStudents,
        Course associatedLabCourse,
        Syllabus syllabus
) {
}
