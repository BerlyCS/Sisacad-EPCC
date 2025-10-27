package com.application.sisacadepcc.domain.model.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Topic {

    @Column(nullable = false)
    private String name;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal weight;

    public Topic() {}

    public Topic(String name, BigDecimal weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() { return name; }
    public BigDecimal getWeight() { return weight; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic topic)) return false;
        return Objects.equals(name, topic.name) &&
                Objects.equals(weight, topic.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weight);
    }
}