package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.Place;
import jakarta.persistence.*;

@Entity
@Table(name = "classrooms")
public class ClassroomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classroom_id")
    private Long classroomId;

    @Embedded
    private Place place;

    public ClassroomEntity() {}

    public ClassroomEntity(Long classroomId, Place place) {
        this.classroomId = classroomId;
        this.place = place;
    }

    // Getters
    public Long getClassroomId() { return classroomId; }
    public Place getPlace() { return place; }
}
