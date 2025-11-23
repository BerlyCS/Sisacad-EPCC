package com.application.sisacadepcc.domain.model.valueobject;

import java.util.Objects;

public class CourseSchedule {

    private String classroomName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    public CourseSchedule() {
    }

    public CourseSchedule(String classroomName, String dayOfWeek, String startTime, String endTime) {
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isComplete() {
        return classroomName != null && !classroomName.isBlank()
                && dayOfWeek != null && !dayOfWeek.isBlank()
                && startTime != null && !startTime.isBlank()
                && endTime != null && !endTime.isBlank();
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