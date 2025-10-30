package com.application.sisacadepcc.domain.model.valueobject;

import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class GeoLocation {

    private Double latitude;
    private Double longitude;

    protected GeoLocation() {}

    public GeoLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double latitude() { return latitude; }
    public Double longitude() { return longitude; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoLocation that)) return false;
        return Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
