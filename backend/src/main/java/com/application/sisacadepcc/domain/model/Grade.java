package com.application.sisacadepcc.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Grade {

    private final Long gradeID;
    private final Long studentID;
    private final Long courseID;
    private final Long professorID;

    private final List<Integer> continuousGrades;
    private final List<Integer> examGrades;

    public Grade(Long gradeID, Long studentID, Long courseID, Long professorID,
                 List<Integer> continuousGrades, List<Integer> examGrades) {
        this.gradeID = gradeID;
        this.studentID = studentID;
        this.courseID = courseID;
        this.professorID = professorID;

        this.continuousGrades = continuousGrades != null
                ? new ArrayList<>(continuousGrades)
                : new ArrayList<>();

        this.examGrades = examGrades != null
                ? new ArrayList<>(examGrades)
                : new ArrayList<>();
    }

    // Getters
    public Long getGradeID() { return gradeID; }
    public Long getStudentID() { return studentID; }
    public Long getCourseID() { return courseID; }
    public Long getProfessorID() { return professorID; }

    public List<Integer> getContinuousGrades() {
        return Collections.unmodifiableList(continuousGrades);
    }

    public List<Integer> getExamGrades() {
        return Collections.unmodifiableList(examGrades);
    }

    public void addContinuousGrade(Integer grade) {
        validateGrade(grade);
        if (continuousGrades.size() < 3) {
            continuousGrades.add(grade);
        }
    }

    public void addExamGrade(Integer grade) {
        validateGrade(grade);
        if (examGrades.size() < 3) {
            examGrades.add(grade);
        }
    }

    private void validateGrade(Integer grade) {
        if (grade == null) {
            throw new IllegalArgumentException("La nota no puede ser nula");
        }

        if (grade < 0 || grade > 20) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 20");
        }
    }
}
