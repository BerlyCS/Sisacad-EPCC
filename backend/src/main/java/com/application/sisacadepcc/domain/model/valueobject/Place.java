package com.application.sisacadepcc.domain.model.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Place {
    private String building;
    private Integer floor;
    private Integer number;
    private Integer capacity;
    private String classroomType;

    public Place(String building, Integer floor, Integer number, Integer capacity, String classroomType) {
        this.building = building;
        this.floor = floor;
        this.number = number;
        this.capacity = capacity;
        this.classroomType = classroomType;
    }

    public Place() {}

    // Getters
    public String getBuilding() { return building; }
    public Integer getFloor() { return floor; }
    public Integer getNumber() { return number; }
    public Integer getCapacity() { return capacity; }
    public String getClassroomType() { return classroomType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Place)) return false;
        Place place = (Place) o;
        return Objects.equals(building, place.building) &&
                Objects.equals(floor, place.floor) &&
                Objects.equals(number, place.number) &&
                Objects.equals(capacity, place.capacity) &&
                Objects.equals(classroomType, place.classroomType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, floor, number, capacity, classroomType);
    }
}
