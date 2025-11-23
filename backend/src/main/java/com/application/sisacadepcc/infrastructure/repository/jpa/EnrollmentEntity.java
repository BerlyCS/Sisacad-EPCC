package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "enrollments")
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_cui", referencedColumnName = "cui", nullable = false)
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_group_id", nullable = false)
    private CourseGroupEntity courseGroup;

    public EnrollmentEntity() {}

    public EnrollmentEntity(StudentEntity student, CourseGroupEntity courseGroup) {
        this.student = student;
        this.courseGroup = courseGroup;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentEntity getStudent() { return student; }
    public void setStudent(StudentEntity student) { this.student = student; }

    public CourseGroupEntity getCourseGroup() { return courseGroup; }
    public void setCourseGroup(CourseGroupEntity courseGroup) { this.courseGroup = courseGroup; }
}
