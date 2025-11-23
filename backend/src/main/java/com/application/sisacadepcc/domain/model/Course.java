package com.application.sisacadepcc.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private Long courseId;
    private int courseCode;
    private String name;
    private int credits;
    private Long syllabusId;
    private int labHours;
    private int practiceHours;
    private int theoryHours;
    private int semesterNumber;
    private List<CourseGroup> groups;

    // Constructor sin parámetros
    public Course() {
        this.groups = new ArrayList<>();
    }

    // Constructor con parámetros
    public Course(int courseCode, String name, int credits, Long syllabusId, int labHours, int practiceHours, int theoryHours, int semesterNumber) {
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.syllabusId = syllabusId;
        this.labHours = labHours;
        this.practiceHours = practiceHours;
        this.theoryHours = theoryHours;
        this.semesterNumber = semesterNumber;
        this.groups = new ArrayList<>();
    }

    // Getters y setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public int getCourseCode() { return courseCode; }
    public void setCourseCode(int courseCode) { this.courseCode = courseCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public Long getSyllabusId() { return syllabusId; }
    public void setSyllabusId(Long syllabusId) { this.syllabusId = syllabusId; }

    public int getLabHours() { return labHours; }
    public void setLabHours(int labHours) { this.labHours = labHours; }

    public int getPracticeHours() { return practiceHours; }
    public void setPracticeHours(int practiceHours) { this.practiceHours = practiceHours; }

    public int getTheoryHours() { return theoryHours; }
    public void setTheoryHours(int theoryHours) { this.theoryHours = theoryHours; }

    public int getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(int semesterNumber) { this.semesterNumber = semesterNumber; }

    public List<CourseGroup> getGroups() { return groups; }
    public void setGroups(List<CourseGroup> groups) { this.groups = groups != null ? groups : new ArrayList<>(); }

    public void addGroup(CourseGroup group) {
        if (group != null && !groups.contains(group)) {
            groups.add(group);
        }
    }

    public void removeGroup(CourseGroup group) {
        groups.remove(group);
    }
}
