package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "courses")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "credit_number")
    private int creditNumber;

    @Column(name = "group_letter")
    private char groupLetter;

    @Column(name = "syllabus_id")
    private Long syllabusId;

    @Column(name = "anio")
    private Integer anio; // Cambiado de año a anio

    @ElementCollection
    @CollectionTable(
            name = "course_enrolled_students",
            joinColumns = @JoinColumn(name = "course_id")
    )
    @Column(name = "student_id")
    private List<Long> enrolledStudentIDs;

    @ElementCollection
    @CollectionTable(
            name = "course_teachers",
            joinColumns = @JoinColumn(name = "course_id")
    )
    @Column(name = "teacher_id")
    private List<Long> teacherIDs;

    public CourseEntity() {}

    public CourseEntity(Long courseId, String name, int creditNumber, char groupLetter, Long syllabusId,
                        Integer anio, List<Long> enrolledStudentIDs, List<Long> teacherIDs) {
        this.courseId = courseId;
        this.name = name;
        this.creditNumber = creditNumber;
        this.groupLetter = groupLetter;
        this.syllabusId = syllabusId;
        this.anio = anio;
        this.enrolledStudentIDs = enrolledStudentIDs;
        this.teacherIDs = teacherIDs;
    }

    // Getters y setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCreditNumber() { return creditNumber; }
    public void setCreditNumber(int creditNumber) { this.creditNumber = creditNumber; }

    public char getGroupLetter() { return groupLetter; }
    public void setGroupLetter(char groupLetter) { this.groupLetter = groupLetter; }

    public Long getSyllabusId() { return syllabusId; }
    public void setSyllabusId(Long syllabusId) { this.syllabusId = syllabusId; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public List<Long> getEnrolledStudentIDs() { return enrolledStudentIDs; }
    public void setEnrolledStudentIDs(List<Long> enrolledStudentIDs) { this.enrolledStudentIDs = enrolledStudentIDs; }

    public List<Long> getTeacherIDs() { return teacherIDs; }
    public void setTeacherIDs(List<Long> teacherIDs) { this.teacherIDs = teacherIDs; }
}
