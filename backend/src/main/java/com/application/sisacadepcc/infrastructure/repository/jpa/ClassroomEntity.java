package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.Place;
import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "classrooms")
public class ClassroomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classroom_id")
    private Long classroomId;

    @Embedded
    private Place place;

    @ElementCollection
    @CollectionTable(
            name = "occupied_schedules",
            joinColumns = @JoinColumn(name = "classroom_id")
    )
    private List<OccupiedSchedule> occupiedSchedules;

    public ClassroomEntity() {}

    public ClassroomEntity(Long classroomId, Place place, List<OccupiedSchedule> occupiedSchedules) {
        this.classroomId = classroomId;
        this.place = place;
        this.occupiedSchedules = occupiedSchedules;
    }

    // Getters
    public Long getClassroomId() { return classroomId; }
    public Place getPlace() { return place; }
    public List<OccupiedSchedule> getOccupiedSchedules() { return occupiedSchedules; }
}
