package com.application.sisacadepcc.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Embeddable
public class OccupiedSchedule {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String dayOfWeek; // Para horarios recurrentes
    private String reservedBy;
    private boolean isRecurring; // true para horarios recurrentes, false para fechas específicas

    // Constructor para fechas específicas
    public OccupiedSchedule(LocalDate date, LocalTime startTime, LocalTime endTime, String reservedBy) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservedBy = reservedBy;
        this.isRecurring = false;
        this.dayOfWeek = calculateDayOfWeek(date);
    }

    // Constructor para horarios recurrentes (días de la semana)
    public OccupiedSchedule(String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek.toUpperCase();
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRecurring = true;
        this.date = null;
        this.reservedBy = null;
    }

    public OccupiedSchedule() {}

    // Getters
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getReservedBy() { return reservedBy; }
    public String getDayOfWeek() {
        if (isRecurring) {
            return dayOfWeek;
        } else if (date != null) {
            return calculateDayOfWeek(date);
        }
        return null;
    }
    public boolean isRecurring() { return isRecurring; }

    // Setters
    public void setDate(LocalDate date) { this.date = date; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setReservedBy(String reservedBy) { this.reservedBy = reservedBy; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    // Método para verificar si este horario ocupa un slot específico
    public boolean occupiesTimeSlot(String targetDayOfWeek, String targetStartTime, String targetEndTime) {
        if (!this.getDayOfWeek().equalsIgnoreCase(targetDayOfWeek)) {
            return false;
        }

        LocalTime targetStart = LocalTime.parse(targetStartTime);
        LocalTime targetEnd = LocalTime.parse(targetEndTime);

        return this.startTime.isBefore(targetEnd) && this.endTime.isAfter(targetStart);
    }

    private String calculateDayOfWeek(LocalDate date) {
        if (date == null) return null;

        return switch (date.getDayOfWeek()) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OccupiedSchedule)) return false;
        OccupiedSchedule that = (OccupiedSchedule) o;
        return isRecurring == that.isRecurring &&
                Objects.equals(date, that.date) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(dayOfWeek, that.dayOfWeek) &&
                Objects.equals(reservedBy, that.reservedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, startTime, endTime, dayOfWeek, reservedBy, isRecurring);
    }

    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if (isRecurring) {
            return String.format("%s %s-%s (Recurrente)", dayOfWeek, startTime.format(timeFormatter), endTime.format(timeFormatter));
        } else {
            return String.format("%s %s-%s", date, startTime.format(timeFormatter), endTime.format(timeFormatter));
        }
    }
}
