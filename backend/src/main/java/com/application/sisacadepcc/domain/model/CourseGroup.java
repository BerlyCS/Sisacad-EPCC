package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.CourseSchedule;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;

import java.util.ArrayList;
import java.util.List;

public class CourseGroup {

    private Long id;
    private String letter;
    private CourseType type;
    private int maxCapacity;
    private int availableCapacity;
    private Course course;
    private List<CourseSchedule> scheduleSlots;
    private List<StudentCourse> enrollments;

    public CourseGroup() {
        this.scheduleSlots = new ArrayList<>();
        this.enrollments = new ArrayList<>();
    }

    public CourseGroup(String letter, CourseType type, int maxCapacity, int availableCapacity, Course course) {
        this.letter = letter;
        this.type = type;
        this.maxCapacity = maxCapacity;
        this.availableCapacity = availableCapacity;
        this.course = course;
        this.scheduleSlots = new ArrayList<>();
        this.enrollments = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type) {
        this.type = type;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(int availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<CourseSchedule> getScheduleSlots() {
        return scheduleSlots;
    }

    public void setScheduleSlots(List<CourseSchedule> scheduleSlots) {
        this.scheduleSlots = scheduleSlots != null ? scheduleSlots : new ArrayList<>();
    }

    public void addScheduleSlot(CourseSchedule slot) {
        if (slot != null && !scheduleSlots.contains(slot)) {
            scheduleSlots.add(slot);
        }
    }

    public void removeScheduleSlot(CourseSchedule slot) {
        scheduleSlots.remove(slot);
    }

    public List<StudentCourse> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<StudentCourse> enrollments) {
        this.enrollments = enrollments != null ? enrollments : new ArrayList<>();
    }
}