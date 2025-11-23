package com.application.sisacadepcc.domain.model;

/**
 * Enrollment links a student (identified by CUI) with a specific course group.
 * CourseGroup carries capacity and schedule details, so we only need to store
 * identifiers here to keep the aggregate lightweight for high-load operations.
 */
public class Enrollment {

    private Long id;
    private String studentCui;
    private Long courseGroupId;

    public Enrollment() {
    }

    public Enrollment(Long id, String studentCui, Long courseGroupId) {
        this.id = id;
        this.studentCui = studentCui;
        this.courseGroupId = courseGroupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentCui() {
        return studentCui;
    }

    public void setStudentCui(String studentCui) {
        this.studentCui = studentCui;
    }

    public Long getCourseGroupId() {
        return courseGroupId;
    }

    public void setCourseGroupId(Long courseGroupId) {
        this.courseGroupId = courseGroupId;
    }
}
