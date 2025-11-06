package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.CourseType;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private Long courseID;
    private String name;
    private int creditNumber;
    private char groupLetter;
    private Long syllabusID;
    private Integer anio;
    private CourseType courseType;
    private Long labPrerequisiteCourseId;
    private List<Long> enrolledStudentIDs;
    private List<Long> teacherIDs;

    // Constructor sin parámetros
    public Course() {
        this.enrolledStudentIDs = new ArrayList<>();
        this.teacherIDs = new ArrayList<>();
        this.courseType = CourseType.THEORY;
    }

    // Constructor con parámetros
    // Getters y setters (necesitas agregar setters)
    public Long getCourseID() { return courseID; }
    public void setCourseID(Long courseID) { this.courseID = courseID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCreditNumber() { return creditNumber; }
    public void setCreditNumber(int creditNumber) { this.creditNumber = creditNumber; }

    public char getGroupLetter() { return groupLetter; }
    public void setGroupLetter(char groupLetter) { this.groupLetter = groupLetter; }

    public Long getSyllabusID() { return syllabusID; }
    public void setSyllabusID(Long syllabusID) { this.syllabusID = syllabusID; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType courseType) {
        this.courseType = courseType != null ? courseType : CourseType.THEORY;
    }

    public Long getLabPrerequisiteCourseId() { return labPrerequisiteCourseId; }
    public void setLabPrerequisiteCourseId(Long labPrerequisiteCourseId) {
        this.labPrerequisiteCourseId = labPrerequisiteCourseId;
    }

    public List<Long> getEnrolledStudentIDs() { return enrolledStudentIDs; }
    public void setEnrolledStudentIDs(List<Long> enrolledStudentIDs) { this.enrolledStudentIDs = enrolledStudentIDs; }

    public List<Long> getTeacherIDs() { return teacherIDs; }
    public void setTeacherIDs(List<Long> teacherIDs) { this.teacherIDs = teacherIDs; }

    public void enrollStudent(Long studentID) {
        if (!enrolledStudentIDs.contains(studentID)) {
            enrolledStudentIDs.add(studentID);
        }
    }

    public void removeStudent(Long studentID) {
        enrolledStudentIDs.remove(studentID);
    }

    public void assignTeacher(Long teacherID) {
        if (!teacherIDs.contains(teacherID)) {
            teacherIDs.add(teacherID);
        }
    }

    public void removeTeacher(Long teacherID) {
        teacherIDs.remove(teacherID);
    }
}
