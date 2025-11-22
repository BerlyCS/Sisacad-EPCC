package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.model.valueobject.Place;

public record ClassroomOptionResponse(
        Long classroomId,
        String name,
        String building,
        Integer floor,
        Integer number,
        Integer capacity,
        String classroomType,
        boolean lab
) {
    public static ClassroomOptionResponse from(Classroom classroom) {
        Place place = classroom.getPlace();
        return new ClassroomOptionResponse(
                classroom.getClassroomID(),
                classroom.getDisplayName(),
                place != null ? place.getBuilding() : null,
                place != null ? place.getFloor() : null,
                place != null ? place.getNumber() : null,
                place != null ? place.getCapacity() : null,
                place != null ? place.getClassroomType() : null,
                classroom.isLab()
        );
    }
}
