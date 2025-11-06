package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.CourseType;
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
    private Integer anio;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type")
    private CourseType courseType;

    @Column(name = "lab_prerequisite_course_id")
    private Long labPrerequisiteCourseId;

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

    public CourseEntity() {
        // Required by JPA
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

        public CourseType getCourseType() { return courseType; }
        public void setCourseType(CourseType courseType) { this.courseType = courseType; }

        public Long getLabPrerequisiteCourseId() { return labPrerequisiteCourseId; }
        public void setLabPrerequisiteCourseId(Long labPrerequisiteCourseId) {
                this.labPrerequisiteCourseId = labPrerequisiteCourseId;
        }

    public List<Long> getEnrolledStudentIDs() { return enrolledStudentIDs; }
    public void setEnrolledStudentIDs(List<Long> enrolledStudentIDs) { this.enrolledStudentIDs = enrolledStudentIDs; }

    public List<Long> getTeacherIDs() { return teacherIDs; }
    public void setTeacherIDs(List<Long> teacherIDs) { this.teacherIDs = teacherIDs; }
}
