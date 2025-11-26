package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "course_code", nullable = true, unique = true)
    private Integer courseCode;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "credits", nullable = true)
    private Integer credits;

    @Column(name = "syllabus_id")
    private Long syllabusId;

    @Column(name = "lab_hours", nullable = true)
    private Integer labHours;

    @Column(name = "practice_hours", nullable = true)
    private Integer practiceHours;

    @Column(name = "theory_hours", nullable = true)
    private Integer theoryHours;

    @Column(name = "semester_number", nullable = true)
    private Integer semesterNumber;

    @Column(name = "continuous_weight_1")
    private Integer continuousWeight1;

    @Column(name = "continuous_weight_2")
    private Integer continuousWeight2;

    @Column(name = "continuous_weight_3")
    private Integer continuousWeight3;

    @Column(name = "exam_weight_1")
    private Integer examWeight1;

    @Column(name = "exam_weight_2")
    private Integer examWeight2;

    @Column(name = "exam_weight_3")
    private Integer examWeight3;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CourseGroupEntity> groups = new ArrayList<>();

    public CourseEntity() {
        // Required by JPA
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(Integer courseCode) {
        this.courseCode = courseCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Long getSyllabusId() {
        return syllabusId;
    }

    public void setSyllabusId(Long syllabusId) {
        this.syllabusId = syllabusId;
    }

    public Integer getLabHours() {
        return labHours;
    }

    public void setLabHours(Integer labHours) {
        this.labHours = labHours;
    }

    public Integer getPracticeHours() {
        return practiceHours;
    }

    public void setPracticeHours(Integer practiceHours) {
        this.practiceHours = practiceHours;
    }

    public Integer getTheoryHours() {
        return theoryHours;
    }

    public void setTheoryHours(Integer theoryHours) {
        this.theoryHours = theoryHours;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public Integer getContinuousWeight1() {
        return continuousWeight1;
    }

    public void setContinuousWeight1(Integer continuousWeight1) {
        this.continuousWeight1 = continuousWeight1;
    }

    public Integer getContinuousWeight2() {
        return continuousWeight2;
    }

    public void setContinuousWeight2(Integer continuousWeight2) {
        this.continuousWeight2 = continuousWeight2;
    }

    public Integer getContinuousWeight3() {
        return continuousWeight3;
    }

    public void setContinuousWeight3(Integer continuousWeight3) {
        this.continuousWeight3 = continuousWeight3;
    }

    public Integer getExamWeight1() {
        return examWeight1;
    }

    public void setExamWeight1(Integer examWeight1) {
        this.examWeight1 = examWeight1;
    }

    public Integer getExamWeight2() {
        return examWeight2;
    }

    public void setExamWeight2(Integer examWeight2) {
        this.examWeight2 = examWeight2;
    }

    public Integer getExamWeight3() {
        return examWeight3;
    }

    public void setExamWeight3(Integer examWeight3) {
        this.examWeight3 = examWeight3;
    }

    public List<CourseGroupEntity> getGroups() {
        return groups;
    }

    public void setGroups(List<CourseGroupEntity> groups) {
        this.groups = groups;
    }
}
