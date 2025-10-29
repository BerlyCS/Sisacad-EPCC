package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "student_course")
public class StudentCourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_documento_identidad", nullable = false)
    private String studentDocumentoIdentidad;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    public StudentCourseEntity() {}

    public StudentCourseEntity(String studentDocumentoIdentidad, Long courseId) {
        this.studentDocumentoIdentidad = studentDocumentoIdentidad;
        this.courseId = courseId;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentDocumentoIdentidad() { return studentDocumentoIdentidad; }
    public void setStudentDocumentoIdentidad(String studentDocumentoIdentidad) { this.studentDocumentoIdentidad = studentDocumentoIdentidad; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
}
