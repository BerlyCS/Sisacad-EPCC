package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.Embeddable;

@Embeddable
public class GeoLocationEmbeddable {

    private Double latitude;
    private Double longitude;

    public GeoLocationEmbeddable() {}

    public GeoLocationEmbeddable(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}