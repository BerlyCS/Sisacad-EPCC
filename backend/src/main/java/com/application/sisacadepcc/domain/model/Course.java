package com.application.sisacadepcc.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Course {

    private Long courseId;
    private Integer courseCode;
    private String name;
    private Integer credits;
    private Long syllabusId;
    private Integer labHours;
    private Integer practiceHours;
    private Integer theoryHours;
    private Integer semesterNumber;
    private List<CourseGroup> groups;
    private List<Integer> continuousGradeWeights;
    private List<Integer> examGradeWeights;

    // Constructor sin parámetros
    public Course() {
        initializeCollections();
    }

    // Constructor con parámetros
    public Course(Integer courseCode, String name, Integer credits, Long syllabusId, Integer labHours, Integer practiceHours, Integer theoryHours, Integer semesterNumber) {
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.syllabusId = syllabusId;
        this.labHours = labHours;
        this.practiceHours = practiceHours;
        this.theoryHours = theoryHours;
        this.semesterNumber = semesterNumber;
        initializeCollections();
    }

    private void initializeCollections() {
        this.groups = new ArrayList<>();
        this.continuousGradeWeights = defaultWeightList();
        this.examGradeWeights = defaultWeightList();
    }

    // Getters y setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Integer getCourseCode() { return courseCode; }
    public void setCourseCode(Integer courseCode) { this.courseCode = courseCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Long getSyllabusId() { return syllabusId; }
    public void setSyllabusId(Long syllabusId) { this.syllabusId = syllabusId; }

    public Integer getLabHours() { return labHours; }
    public void setLabHours(Integer labHours) { this.labHours = labHours; }

    public Integer getPracticeHours() { return practiceHours; }
    public void setPracticeHours(Integer practiceHours) { this.practiceHours = practiceHours; }

    public Integer getTheoryHours() { return theoryHours; }
    public void setTheoryHours(Integer theoryHours) { this.theoryHours = theoryHours; }

    public Integer getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(Integer semesterNumber) { this.semesterNumber = semesterNumber; }

    public List<CourseGroup> getGroups() { return groups; }
    public void setGroups(List<CourseGroup> groups) { this.groups = groups != null ? groups : new ArrayList<>(); }

    public List<Integer> getContinuousGradeWeights() {
        return Collections.unmodifiableList(continuousGradeWeights);
    }

    public void setContinuousGradeWeights(List<Integer> weights) {
        this.continuousGradeWeights = normalizeWeights(weights);
    }

    public List<Integer> getExamGradeWeights() {
        return Collections.unmodifiableList(examGradeWeights);
    }

    public void setExamGradeWeights(List<Integer> weights) {
        this.examGradeWeights = normalizeWeights(weights);
    }

    public void addGroup(CourseGroup group) {
        if (group != null && !groups.contains(group)) {
            groups.add(group);
        }
    }

    public void removeGroup(CourseGroup group) {
        groups.remove(group);
    }

    private List<Integer> normalizeWeights(List<Integer> source) {
        if (source == null || source.isEmpty()) {
            return defaultWeightList();
        }
        List<Integer> normalized = defaultWeightList();
        for (int i = 0; i < normalized.size() && i < source.size(); i++) {
            Integer value = source.get(i);
            normalized.set(i, value != null && value > 0 ? value : 0);
        }
        return normalized;
    }

    private List<Integer> defaultWeightList() {
        List<Integer> defaults = new ArrayList<>(3);
        defaults.add(0);
        defaults.add(0);
        defaults.add(0);
        return defaults;
    }
}
