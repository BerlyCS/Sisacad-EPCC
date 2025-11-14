package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.presentation.dto.CourseDetailsResponse;
import com.application.sisacadepcc.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(service.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailsResponse> getCourseDetails(@PathVariable Long id) {
        return service.getCourseDetails(id)
                .map(CourseDetailsResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequiresAdministratorAccess
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        // Lógica para crear curso
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{id}")
    @RequiresAdministratorAccess
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        // Lógica para actualizar curso
        return ResponseEntity.ok(course);
    }

    @DeleteMapping("/{id}")
    @RequiresAdministratorAccess
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        // Lógica para eliminar curso
        return ResponseEntity.noContent().build();
    }
}