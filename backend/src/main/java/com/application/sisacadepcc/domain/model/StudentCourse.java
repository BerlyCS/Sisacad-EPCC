package com.application.sisacadepcc.domain.model;

public class StudentCourse {
    private Long id;
    private String studentDocumentoIdentidad;
    private Long courseId;

    public StudentCourse() {}

    public StudentCourse(Long id, String studentDocumentoIdentidad, Long courseId) {
        this.id = id;
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
