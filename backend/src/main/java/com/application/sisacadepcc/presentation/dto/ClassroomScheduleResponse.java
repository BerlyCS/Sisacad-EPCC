package com.application.sisacadepcc.presentation.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassroomScheduleResponse {

    private String classroomName;
    private Map<String, List<ScheduleEntry>> schedule = new HashMap<>();

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public Map<String, List<ScheduleEntry>> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<String, List<ScheduleEntry>> schedule) {
        this.schedule = schedule;
    }

    public void addEntry(String dayOfWeek, ScheduleEntry entry) {
        schedule.computeIfAbsent(dayOfWeek, key -> new ArrayList<>()).add(entry);
    }

    public static class ScheduleEntry {
        private String startTime;
        private String endTime;
        private String courseName;
        private String type;

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

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
