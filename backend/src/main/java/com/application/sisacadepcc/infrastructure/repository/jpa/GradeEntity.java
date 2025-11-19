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

    @Column(name = "student_documento_identidad", nullable = false)
    private String studentDocumentoIdentidad;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

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

    public GradeEntity(String studentDocumentoIdentidad, String courseCode, Long professorID,
                       List<Integer> continuousGrades, List<Integer> examGrades) {
        this.studentDocumentoIdentidad = studentDocumentoIdentidad;
        this.courseCode = courseCode;
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

    public String getStudentDocumentoIdentidad() {
        return studentDocumentoIdentidad;
    }

    public void setStudentDocumentoIdentidad(String studentDocumentoIdentidad) {
        this.studentDocumentoIdentidad = studentDocumentoIdentidad;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
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