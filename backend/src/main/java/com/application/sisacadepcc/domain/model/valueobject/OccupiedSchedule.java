package com.application.sisacadepcc.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Embeddable
public class OccupiedSchedule {
    private Date date;
    private String startTime; // Cambiado de Time a String
    private String endTime;   // Cambiado de Time a String
    private String dayOfWeek; // Para horarios recurrentes
    private String reservedBy;
    private boolean isRecurring; // true para horarios recurrentes, false para fechas específicas

    // Constructor para fechas específicas
    public OccupiedSchedule(Date date, String startTime, String endTime, String reservedBy) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservedBy = reservedBy;
        this.isRecurring = false;
        this.dayOfWeek = calculateDayOfWeek(date);
    }

    // Constructor para horarios recurrentes (días de la semana)
    public OccupiedSchedule(String dayOfWeek, String startTime, String endTime) {
        this.dayOfWeek = dayOfWeek.toUpperCase();
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRecurring = true;
        this.date = null;
        this.reservedBy = null;
    }

    public OccupiedSchedule() {}

    // Getters
    public Date getDate() { return date; }
    public String getStartTime() { return startTime; } // Cambiado de Time a String
    public String getEndTime() { return endTime; }     // Cambiado de Time a String
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
    public void setDate(Date date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setReservedBy(String reservedBy) { this.reservedBy = reservedBy; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    // Método para verificar si este horario coincide con un día y hora específicos
    public boolean matches(String targetDayOfWeek, String targetStartTime, String targetEndTime) {
        if (!this.getDayOfWeek().equalsIgnoreCase(targetDayOfWeek)) {
            return false;
        }

        return isTimeOverlap(
                this.startTime,
                this.endTime,
                targetStartTime,
                targetEndTime
        );
    }

    // Método para verificar si este horario ocupa un slot específico
    public boolean occupiesTimeSlot(String targetDayOfWeek, String targetStartTime, String targetEndTime) {
        return matches(targetDayOfWeek, targetStartTime, targetEndTime);
    }

    private String calculateDayOfWeek(Date date) {
        if (date == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.MONDAY: return "LUNES";
            case Calendar.TUESDAY: return "MARTES";
            case Calendar.WEDNESDAY: return "MIERCOLES";
            case Calendar.THURSDAY: return "JUEVES";
            case Calendar.FRIDAY: return "VIERNES";
            case Calendar.SATURDAY: return "SABADO";
            case Calendar.SUNDAY: return "DOMINGO";
            default: return null;
        }
    }

    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        // Normalizar formatos de tiempo
        String normStart1 = normalizeTime(start1);
        String normEnd1 = normalizeTime(end1);
        String normStart2 = normalizeTime(start2);
        String normEnd2 = normalizeTime(end2);

        return normStart1.compareTo(normEnd2) < 0 && normEnd1.compareTo(normStart2) > 0;
    }

    private String normalizeTime(String time) {
        // Asegurar formato HH:MM
        if (time == null) return "";
        if (time.length() == 4) { // H:MM
            return "0" + time;
        }
        return time.length() == 5 ? time : time;
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
        if (isRecurring) {
            return String.format("%s %s-%s (Recurrente)", dayOfWeek, startTime, endTime);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return String.format("%s %s-%s", dateFormat.format(date), startTime, endTime);
        }
    }
}
