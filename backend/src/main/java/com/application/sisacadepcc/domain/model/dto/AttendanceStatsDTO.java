package com.application.sisacadepcc.domain.model.dto;

public class AttendanceStatsDTO {
    private double attendancePercentage;
    private double syllabusProgress;
    private int totalSessions;
    private int presentStudents;
    private int absentStudents;

    public AttendanceStatsDTO(double attendancePercentage, double syllabusProgress, int totalSessions, int presentStudents, int absentStudents) {
        this.attendancePercentage = attendancePercentage;
        this.syllabusProgress = syllabusProgress;
        this.totalSessions = totalSessions;
        this.presentStudents = presentStudents;
        this.absentStudents = absentStudents;
    }

    public double getAttendancePercentage() { return attendancePercentage; }
    public double getSyllabusProgress() { return syllabusProgress; }
    public int getTotalSessions() { return totalSessions; }
    public int getPresentStudents() { return presentStudents; }
    public int getAbsentStudents() { return absentStudents; }
}
