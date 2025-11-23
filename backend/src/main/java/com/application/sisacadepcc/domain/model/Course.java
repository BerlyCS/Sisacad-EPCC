package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.CourseScheduleSlot;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import java.math.BigDecimal;
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
    private int anio;
    private int creditNumber;
    private Long labPrerequisiteCourseId;
    private Integer labCapacity;
    private List<Long> teacherIDs;
    private CourseType courseType;
    private char groupLetter;
    private List<CourseGroup> groups;
    private List<BigDecimal> continuousGradeWeights;
    private List<BigDecimal> examGradeWeights;
    private List<CourseScheduleSlot> scheduleSlots;

    // Constructor sin parámetros
    public Course() {
        this.groups = new ArrayList<>();
        this.teacherIDs = new ArrayList<>();
        this.continuousGradeWeights = new ArrayList<>();
        this.examGradeWeights = new ArrayList<>();
        this.scheduleSlots = new ArrayList<>();
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
        this.continuousGradeWeights = new ArrayList<>();
        this.examGradeWeights = new ArrayList<>();
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

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getCreditNumber() { return creditNumber; }
    public void setCreditNumber(int creditNumber) { this.creditNumber = creditNumber; }

    public Long getLabPrerequisiteCourseId() { return labPrerequisiteCourseId; }
    public void setLabPrerequisiteCourseId(Long labPrerequisiteCourseId) { this.labPrerequisiteCourseId = labPrerequisiteCourseId; }

    public Integer getLabCapacity() { return labCapacity; }
    public void setLabCapacity(Integer labCapacity) { this.labCapacity = labCapacity; }

    public List<Long> getTeacherIDs() { return teacherIDs; }
    public void setTeacherIDs(List<Long> teacherIDs) { this.teacherIDs = teacherIDs; }

    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType courseType) { this.courseType = courseType; }

    public char getGroupLetter() { return groupLetter; }
    public void setGroupLetter(char groupLetter) { this.groupLetter = groupLetter; }

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

    public List<BigDecimal> getContinuousGradeWeights() { return continuousGradeWeights; }
    public void setContinuousGradeWeights(List<BigDecimal> continuousGradeWeights) { this.continuousGradeWeights = continuousGradeWeights; }

    public List<BigDecimal> getExamGradeWeights() { return examGradeWeights; }
    public void setExamGradeWeights(List<BigDecimal> examGradeWeights) { this.examGradeWeights = examGradeWeights; }

    public int getEffectiveLabCapacity() {
        return labCapacity != null ? labCapacity : 0;
    }

    public List<CourseScheduleSlot> getScheduleSlots() {
        return scheduleSlots;
    }

    public void setScheduleSlots(List<CourseScheduleSlot> scheduleSlots) {
        this.scheduleSlots = scheduleSlots != null ? scheduleSlots : new ArrayList<>();
    }
}
