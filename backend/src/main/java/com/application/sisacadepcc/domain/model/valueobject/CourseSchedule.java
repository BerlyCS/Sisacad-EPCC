package com.application.sisacadepcc.domain.model.valueobject;

import java.time.LocalTime;
import java.util.Objects;

public class CourseSchedule {

    private String classroomName;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public CourseSchedule() {
    }

    public CourseSchedule(String classroomName, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.classroomName = classroomName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isComplete() {
        return classroomName != null && !classroomName.isBlank()
                && dayOfWeek != null && !dayOfWeek.isBlank()
                && startTime != null
                && endTime != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseSchedule that)) return false;
        return Objects.equals(classroomName, that.classroomName)
                && Objects.equals(dayOfWeek, that.dayOfWeek)
                && Objects.equals(startTime, that.startTime)
                && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classroomName, dayOfWeek, startTime, endTime);
    }
}