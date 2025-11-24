package com.application.sisacadepcc.presentation.dto;

import java.util.ArrayList;
import java.util.List;

public class WeeklyAvailabilityResponse {

    private String classroomName;
    private List<String> days = new ArrayList<>();
    private List<String> timeSlots = new ArrayList<>();
    private List<List<Boolean>> availability = new ArrayList<>();

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public List<String> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<String> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public List<List<Boolean>> getAvailability() {
        return availability;
    }

    public void setAvailability(List<List<Boolean>> availability) {
        this.availability = availability;
    }
}
