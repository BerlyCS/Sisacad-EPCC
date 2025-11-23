package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_groups")
public class CourseGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(name = "group_letter", nullable = false, length = 2)
    private String letter;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false)
    private CourseType type;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "available_capacity", nullable = false)
    private int availableCapacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "course_group_schedule_slots",
            joinColumns = @JoinColumn(name = "group_id")
    )
    private List<CourseScheduleEmbeddable> scheduleSlots = new ArrayList<>();

    @OneToMany(mappedBy = "courseGroup", fetch = FetchType.LAZY)
    private List<EnrollmentEntity> enrollments = new ArrayList<>();

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

    public CourseEntity getCourse() {
        return course;
    }

    public void setCourse(CourseEntity course) {
        this.course = course;
    }

    public List<CourseScheduleEmbeddable> getScheduleSlots() {
        return scheduleSlots;
    }

    public void setScheduleSlots(List<CourseScheduleEmbeddable> scheduleSlots) {
        this.scheduleSlots = scheduleSlots;
    }

    public List<EnrollmentEntity> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<EnrollmentEntity> enrollments) {
        this.enrollments = enrollments;
    }
}
