package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;

@Entity(name = "CourseSchedule")
@Table(name = "course_group_schedules")
public class CourseScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_group_id", nullable = false)
    private CourseGroupEntity courseGroup;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private ClassroomEntity classroom;

    // Ordering for presentation — lower numbers appear first
    @Column(name = "sequence_order")
    private Integer sequence;

    public CourseScheduleEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseGroupEntity getCourseGroup() {
        return courseGroup;
    }

    public void setCourseGroup(CourseGroupEntity courseGroup) {
        this.courseGroup = courseGroup;
        if (courseGroup != null) {
            if (courseGroup.getCourse() != null) {
                this.courseId = courseGroup.getCourse().getCourseId();
            } else if (courseGroup.getCourseId() != null) {
                this.courseId = courseGroup.getCourseId();
            }
        }
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public ScheduleEntity getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleEntity schedule) {
        this.schedule = schedule;
    }

    public ClassroomEntity getClassroom() {
        return classroom;
    }

    public void setClassroom(ClassroomEntity classroom) {
        this.classroom = classroom;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }
}
