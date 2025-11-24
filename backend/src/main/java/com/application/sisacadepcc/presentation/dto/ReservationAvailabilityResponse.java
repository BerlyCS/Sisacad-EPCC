package com.application.sisacadepcc.presentation.dto;

public class ReservationAvailabilityResponse {

    private boolean available;

    public ReservationAvailabilityResponse() {
    }

    public ReservationAvailabilityResponse(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
