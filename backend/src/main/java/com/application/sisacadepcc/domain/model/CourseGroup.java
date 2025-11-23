package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.CourseSchedule;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CourseGroup {

    private Long id;
    private String letter;
    private Long teacherId;
    private CourseType type;
    private int maxCapacity;
    private int availableCapacity;
    private Course course;
    private Long courseId;
    private List<CourseSchedule> schedules;
    private List<Enrollment> enrollments;

    public CourseGroup() {
        this.schedules = new ArrayList<>();
        this.enrollments = new ArrayList<>();
    }

    public CourseGroup(String letter, CourseType type, int maxCapacity, int availableCapacity, Course course) {
        this();
        this.letter = letter;
        this.type = type;
        this.maxCapacity = maxCapacity;
        this.availableCapacity = availableCapacity;
        this.course = course;
        this.courseId = course != null ? course.getCourseId() : null;
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

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
        this.courseId = course != null ? course.getCourseId() : null;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public List<CourseSchedule> getSchedules() {
        return Collections.unmodifiableList(schedules);
    }

    public List<CourseSchedule> getScheduleSlots() {
        return getSchedules();
    }

    public void setSchedules(List<CourseSchedule> schedules) {
        this.schedules = schedules != null ? new ArrayList<>(schedules) : new ArrayList<>();
    }

    public void setScheduleSlots(List<CourseSchedule> scheduleSlots) {
        setSchedules(scheduleSlots);
    }

    public void addScheduleSlot(CourseSchedule slot) {
        if (slot != null && !schedules.contains(slot)) {
            schedules.add(slot);
        }
    }

    public void removeScheduleSlot(CourseSchedule slot) {
        schedules.remove(slot);
    }

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments != null ? new ArrayList<>(enrollments) : new ArrayList<>();
    }

    public void addEnrollment(Enrollment enrollment) {
        if (enrollment != null && !enrollments.contains(enrollment)) {
            enrollments.add(enrollment);
        }
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseGroup that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}