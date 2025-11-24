package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorOrSecretaryAccess;
import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.model.valueobject.Place;
import com.application.sisacadepcc.service.ClassroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService service;

    public ClassroomController(ClassroomService service) {
        this.service = service;
    }

    @GetMapping
    @RequiresAdministratorOrSecretaryAccess
    public ResponseEntity<List<Classroom>> getAllClassrooms() {
        return ResponseEntity.ok(service.getAllClassrooms());
    }

    @PostMapping
    @RequiresAdministratorOrSecretaryAccess
    public ResponseEntity<Classroom> createClassroom(@RequestBody CreateClassroomRequest request) {
        Place place = new Place(request.place.building, request.place.floor, request.place.number, request.place.capacity, request.place.classroomType);
        Classroom classroom = new Classroom(place);
        Classroom created = service.createClassroom(classroom);
        return ResponseEntity.ok(created);
    }

    public static class CreateClassroomRequest {
        public PlaceData place;
    }

    public static class PlaceData {
        public String building;
        public Integer floor;
        public Integer number;
        public Integer capacity;
        public String classroomType;
    }

    @PutMapping("/{id}")
    @RequiresAdministratorOrSecretaryAccess
    public ResponseEntity<Classroom> updateClassroom(@PathVariable Long id, @RequestBody Classroom classroom) {
        // Lógica para actualizar aula
        return ResponseEntity.ok(classroom);
    }

    @DeleteMapping("/{id}")
    @RequiresAdministratorOrSecretaryAccess
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        // Lógica para eliminar aula
        return ResponseEntity.noContent().build();
    }
}