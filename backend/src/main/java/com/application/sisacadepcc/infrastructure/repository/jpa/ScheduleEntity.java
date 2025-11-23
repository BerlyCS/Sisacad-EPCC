package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;

import java.util.Optional;

import com.application.sisacadepcc.domain.model.valueobject.Place;

@Entity
@Table(name = "schedules")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @Column(name = "classroom_name")
    private String classroomName;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_group_id", nullable = false)
    private CourseGroupEntity courseGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private ClassroomEntity classroom;

    // Constructors
    public ScheduleEntity() {}

    public ScheduleEntity(String classroomName, String dayOfWeek, String startTime, String endTime, CourseGroupEntity courseGroup) {
        this.classroomName = classroomName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseGroup = courseGroup;
    }

    public ScheduleEntity(ClassroomEntity classroom, String dayOfWeek, String startTime, String endTime, CourseGroupEntity courseGroup) {
        this.classroom = classroom;
        this.classroomName = classroom != null ? buildDisplayName(classroom) : null;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseGroup = courseGroup;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassroomName() {
        if (classroom != null) {
            return buildDisplayName(classroom);
        }
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

    public CourseGroupEntity getCourseGroup() {
        return courseGroup;
    }

    public void setCourseGroup(CourseGroupEntity courseGroup) {
        this.courseGroup = courseGroup;
    }

    public ClassroomEntity getClassroom() {
        return classroom;
    }

    public void setClassroom(ClassroomEntity classroom) {
        this.classroom = classroom;
    }

    private String buildDisplayName(ClassroomEntity classroom) {
        if (classroom == null) {
            return null;
        }
        Place place = classroom.getPlace();
        if (place == null) {
            return "Aula " + Optional.ofNullable(classroom.getClassroomId()).orElse(null);
        }
        String building = Optional.ofNullable(place.getBuilding()).orElse("").trim();
        Integer number = place.getNumber();
        if (!building.isEmpty() && number != null) {
            return building + " " + number;
        }
        if (number != null) {
            return "Aula " + number;
        }
        return "Aula " + Optional.ofNullable(classroom.getClassroomId()).orElse(null);
    }
}