package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grades")
public class GradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long gradeID;

    @Column(name = "student_id", nullable = false)
    private Long studentID;

    @Column(name = "course_id", nullable = false)
    private Long courseID;

    @Column(name = "professor_id", nullable = false)
    private Long professorID;

    // --- List of continuous grades ---
    @ElementCollection
    @CollectionTable(
            name = "grade_continuous_grades",
            joinColumns = @JoinColumn(name = "grade_id")
    )
    @Column(name = "grade_value")
    private List<Integer> continuousGrades = new ArrayList<>();

    // --- List of exam grades ---
    @ElementCollection
    @CollectionTable(
            name = "grade_exam_grades",
            joinColumns = @JoinColumn(name = "grade_id")
    )
    @Column(name = "grade_value")
    private List<Integer> examGrades = new ArrayList<>();

    // --- Constructors ---
    public GradeEntity() {}

    public GradeEntity(Long studentID, Long courseID, Long professorID,
                       List<Integer> continuousGrades, List<Integer> examGrades) {
        this.studentID = studentID;
        this.courseID = courseID;
        this.professorID = professorID;
        if (continuousGrades != null) this.continuousGrades = continuousGrades;
        if (examGrades != null) this.examGrades = examGrades;
    }

    // --- Getters & Setters ---
    public Long getGradeID() {
        return gradeID;
    }

    public void setGradeID(Long gradeID) {
        this.gradeID = gradeID;
    }

    public Long getStudentID() {
        return studentID;
    }

    public void setStudentID(Long studentID) {
        this.studentID = studentID;
    }

    public Long getCourseID() {
        return courseID;
    }

    public void setCourseID(Long courseID) {
        this.courseID = courseID;
    }

    public Long getProfessorID() {
        return professorID;
    }

    public void setProfessorID(Long professorID) {
        this.professorID = professorID;
    }

    public List<Integer> getContinuousGrades() {
        return continuousGrades;
    }

    public void setContinuousGrades(List<Integer> continuousGrades) {
        this.continuousGrades = continuousGrades;
    }

    public List<Integer> getExamGrades() {
        return examGrades;
    }

    public void setExamGrades(List<Integer> examGrades) {
        this.examGrades = examGrades;
    }
}