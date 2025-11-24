package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.Place;

import java.util.Locale;
import java.util.Optional;

public class Classroom {

    private final Long classroomID;
    private final Place place;

    public Classroom(Long classroomID, Place place) {
        this.classroomID = classroomID;
        this.place = place;
    }

    // Getters
    public Long getClassroomID() { return classroomID; }
    public Place getPlace() { return place; }

    // Método para obtener el nombre del aula - CORREGIDO
    public String getName() {
        // Si Place no tiene getName(), usa un valor por defecto
        return "Aula " + classroomID;
    }

    public String getDisplayName() {
        if (place == null) {
            return getName();
        }

        String building = Optional.ofNullable(place.getBuilding()).map(String::trim).orElse("");
        Integer number = place.getNumber();
        String normalizedBuilding = building.toUpperCase(Locale.ROOT);

        if (isLabBuilding(normalizedBuilding) && number != null) {
            return formatLabName(normalizedBuilding, number);
        }

        if (number != null) {
            if (normalizedBuilding.contains("AULA")) {
                return "AULA " + number;
            }
            if (!building.isBlank()) {
                return building + " " + number;
            }
            return "AULA " + number;
        }

        if (!building.isBlank()) {
            return building;
        }
        return getName();
    }

    public String getClassroomType() {
        return place != null ? place.getClassroomType() : null;
    }

    public Integer getCapacity() {
        return place != null ? place.getCapacity() : null;
    }

    public boolean isLab() {
        String type = Optional.ofNullable(getClassroomType()).orElse("");
        if (!type.isBlank() && type.toUpperCase(Locale.ROOT).contains("LAB")) {
            return true;
        }
        String name = getDisplayName();
        return name != null && name.toUpperCase(Locale.ROOT).contains("LAB");
    }

    public String getNormalizedDisplayName() {
        return normalizeName(getDisplayName());
    }

    private boolean isLabBuilding(String building) {
        return building.contains("LAB");
    }

    private String formatLabName(String building, int number) {
        String prefix = building.isBlank() ? "LAB" : (building.contains("LAB") ? "LAB" : building.trim());
        return prefix + " " + String.format(Locale.ROOT, "%02d", number);
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
