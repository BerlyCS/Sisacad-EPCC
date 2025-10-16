package com.application.sisacadepcc.domain.model.valueobject;

import jakarta.persistence.Embeddable;

import java.sql.Time;
import java.util.Date;
import java.util.Objects;

@Embeddable
public class OccupiedSchedule {
    private Date date;
    private Time startTime;
    private Time endTime;
    private String reservedBy;

    public OccupiedSchedule(Date date, Time startTime, Time endTime, String reservedBy) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservedBy = reservedBy;
    }

    public OccupiedSchedule() {}

    // Getters
    public Date getDate() { return date; }
    public Time getStartTime() { return startTime; }
    public Time getEndTime() { return endTime; }
    public String getReservedBy() { return reservedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OccupiedSchedule)) return false;
        OccupiedSchedule that = (OccupiedSchedule) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(reservedBy, that.reservedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, startTime, endTime, reservedBy);
    }
}
