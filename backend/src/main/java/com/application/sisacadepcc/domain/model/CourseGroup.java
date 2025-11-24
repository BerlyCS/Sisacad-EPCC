package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.CourseType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CourseGroup {

    private Long id;
    private String letter;
    private Long teacherId;
    private CourseType type;
    private int maxCapacity;
    private int availableCapacity;
    private Course course;
    private Long courseId;
    private List<CourseSchedule> courseSchedules;
    private List<Enrollment> enrollments;

    public CourseGroup() {
        this.courseSchedules = new ArrayList<>();
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

    public List<CourseSchedule> getCourseSchedules() {
        return Collections.unmodifiableList(courseSchedules);
    }

    public List<Schedule> getScheduleSlots() {
        return courseSchedules.stream()
                .map(CourseSchedule::getSchedule)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }

    public void setSchedules(List<Schedule> schedules) {
        if (schedules == null) {
            this.courseSchedules = new ArrayList<>();
            return;
        }
        this.courseSchedules = schedules.stream()
                .filter(Objects::nonNull)
                .map(schedule -> {
                    CourseSchedule assignment = new CourseSchedule();
                    assignment.setSchedule(schedule);
                    return assignment;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void setScheduleSlots(List<Schedule> scheduleSlots) {
        setSchedules(scheduleSlots);
    }

    public void setCourseSchedules(List<CourseSchedule> courseSchedules) {
        this.courseSchedules = courseSchedules != null ? new ArrayList<>(courseSchedules) : new ArrayList<>();
    }

    public void addScheduleSlot(Schedule slot) {
        if (slot == null) {
            return;
        }
        CourseSchedule assignment = new CourseSchedule();
        assignment.setSchedule(slot);
        addCourseSchedule(assignment);
    }

    public void removeScheduleSlot(Schedule slot) {
        if (slot == null) {
            return;
        }
        courseSchedules.removeIf(cs -> slot.equals(cs.getSchedule()));
    }

    public void addCourseSchedule(CourseSchedule courseSchedule) {
        if (courseSchedule != null && !courseSchedules.contains(courseSchedule)) {
            courseSchedules.add(courseSchedule);
        }
    }

    public void removeCourseSchedule(CourseSchedule courseSchedule) {
        courseSchedules.remove(courseSchedule);
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