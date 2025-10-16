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
                        List<Long> enrolledStudentIDs, List<Long> teacherIDs) {
        this.courseId = courseId;
        this.name = name;
        this.creditNumber = creditNumber;
        this.groupLetter = groupLetter;
        this.syllabusId = syllabusId;
        this.enrolledStudentIDs = enrolledStudentIDs;
        this.teacherIDs = teacherIDs;
    }

    // Getters
    public Long getCourseId() { return courseId; }
    public String getName() { return name; }
    public int getCreditNumber() { return creditNumber; }
    public char getGroupLetter() { return groupLetter; }
    public Long getSyllabusId() { return syllabusId; }
    public List<Long> getEnrolledStudentIDs() { return enrolledStudentIDs; }
    public List<Long> getTeacherIDs() { return teacherIDs; }
}