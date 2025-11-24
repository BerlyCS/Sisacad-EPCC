package com.application.sisacadepcc.domain.model;

import java.util.Objects;

public class CourseSchedule {

    private Long id;
    private Long courseGroupId;
    private Long courseId;
    private Schedule schedule;
    private Long classroomId;
    private Integer sequenceOrder;

    public CourseSchedule() {
    }

    public CourseSchedule(Long id,
                          Long courseGroupId,
                          Long courseId,
                          Schedule schedule,
                          Long classroomId,
                          Integer sequenceOrder) {
        this.id = id;
        this.courseGroupId = courseGroupId;
        this.courseId = courseId;
        this.schedule = schedule;
        this.classroomId = classroomId;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseGroupId() {
        return courseGroupId;
    }

    public void setCourseGroupId(Long courseGroupId) {
        this.courseGroupId = courseGroupId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseSchedule that)) return false;
        if (this.id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }
}
