package com.application.sisacadepcc.domain.model;

import java.util.List;

public class Course {

    private final Long courseID;
    private final String name;
    private final int creditNumber;
    private final char groupLetter;
    private final Long syllabusID;

    private final List<Long> enrolledStudentIDs;
    private final List<Long> teacherIDs;

    public Course(Long courseID, String name, int creditNumber, char groupLetter, Long syllabusID,
                  List<Long> enrolledStudentIDs, List<Long> teacherIDs) {
        this.courseID = courseID;
        this.name = name;
        this.creditNumber = creditNumber;
        this.groupLetter = groupLetter;
        this.syllabusID = syllabusID;
        this.enrolledStudentIDs = enrolledStudentIDs;
        this.teacherIDs = teacherIDs;
    }

    // Getters
    public Long getCourseID() { return courseID; }
    public String getName() { return name; }
    public int getCreditNumber() { return creditNumber; }
    public char getGroupLetter() { return groupLetter; }
    public Long getSyllabusID() { return syllabusID; }
    public List<Long> getEnrolledStudentIDs() { return enrolledStudentIDs; }
    public List<Long> getTeacherIDs() { return teacherIDs; }

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
