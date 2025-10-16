package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.Place;
import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;

import java.sql.Time;
import java.util.Date;
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

    public boolean isAvailable(Date date, Time start, Time end) {
        return occupiedSchedules.stream()
                .noneMatch(s -> s.getDate().equals(date)
                        && (start.before(s.getEndTime()) && end.after(s.getStartTime())));
    }
}
