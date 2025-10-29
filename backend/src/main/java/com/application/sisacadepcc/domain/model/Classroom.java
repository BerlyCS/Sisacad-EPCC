package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.Place;
import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;

import java.util.List;

public class Classroom {

    private final Long classroomID;
    private final Place place;
    private final List<OccupiedSchedule> occupiedSchedules;

    public Classroom(Long classroomID, Place place, List<OccupiedSchedule> occupiedSchedules) {
        this.classroomID = classroomID;
        this.place = place;
        this.occupiedSchedules = occupiedSchedules;
    }

    // Getters
    public Long getClassroomID() { return classroomID; }
    public Place getPlace() { return place; }
    public List<OccupiedSchedule> getOccupiedSchedules() { return occupiedSchedules; }

    public void reserve(OccupiedSchedule newSchedule) {
        occupiedSchedules.add(newSchedule);
    }

    // Método simplificado - usa strings como el resto del proyecto
    public boolean isAvailable(String dayOfWeek, String startTime, String endTime) {
        return occupiedSchedules.stream()
                .noneMatch(schedule -> schedule.occupiesTimeSlot(dayOfWeek, startTime, endTime));
    }

    // Método para obtener el nombre del aula - CORREGIDO
    public String getName() {
        // Si Place no tiene getName(), usa un valor por defecto
        return "Aula " + classroomID;
    }
}
