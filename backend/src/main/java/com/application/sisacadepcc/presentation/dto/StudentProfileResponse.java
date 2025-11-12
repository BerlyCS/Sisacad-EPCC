package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Student;

import java.util.List;

public class StudentProfileResponse {
    private final Student student;
    private final List<Course> courses;
    private final List<StudentScheduleEntry> schedule;

    public StudentProfileResponse(Student student,
                                  List<Course> courses,
                                  List<StudentScheduleEntry> schedule) {
        this.student = student;
        this.courses = courses;
        this.schedule = schedule;
    }

    public Student getStudent() {
        return student;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public List<StudentScheduleEntry> getSchedule() {
        return schedule;
    }
}
