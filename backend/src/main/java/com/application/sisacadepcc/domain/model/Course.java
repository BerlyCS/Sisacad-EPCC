package com.application.sisacadepcc.domain.model;

import java.util.List;
import java.util.ArrayList;

public class Course {

    private Long courseID;
    private String name;
    private int creditNumber;
    private char groupLetter;
    private Long syllabusID;
    private Integer anio;
    private List<Long> enrolledStudentIDs;
    private List<Long> teacherIDs;

    // Constructor sin parámetros
    public Course() {
        this.enrolledStudentIDs = new ArrayList<>();
        this.teacherIDs = new ArrayList<>();
    }

    // Constructor con parámetros
    public Course(Long courseID, String name, int creditNumber, char groupLetter, Long syllabusID,
                  Integer anio, List<Long> enrolledStudentIDs, List<Long> teacherIDs) {
        this.courseID = courseID;
        this.name = name;
        this.creditNumber = creditNumber;
        this.groupLetter = groupLetter;
        this.syllabusID = syllabusID;
        this.anio = anio;
        this.enrolledStudentIDs = enrolledStudentIDs != null ? enrolledStudentIDs : new ArrayList<>();
        this.teacherIDs = teacherIDs != null ? teacherIDs : new ArrayList<>();
    }

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
