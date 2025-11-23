package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.presentation.dto.CourseDetailsResponse;
import com.application.sisacadepcc.presentation.dto.UpdateLabCapacityRequest;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.CourseService;
import com.application.sisacadepcc.service.UserRole;
import com.application.sisacadepcc.service.ExcelScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;
    private final AuthorizationService authorizationService;
    private final ExcelScheduleService excelScheduleService;

    public CourseController(CourseService service,
                            AuthorizationService authorizationService,
                            ExcelScheduleService excelScheduleService) {
        this.service = service;
        this.authorizationService = authorizationService;
        this.excelScheduleService = excelScheduleService;
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

    @PatchMapping("/{courseId}/lab-capacity")
    public ResponseEntity<Course> updateLabCapacity(@PathVariable Long courseId,
                                                    @RequestBody UpdateLabCapacityRequest request,
                                                    Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        Integer newCapacity = request != null ? request.labCapacity() : null;
        if (newCapacity != null && newCapacity < 0) {
            return ResponseEntity.badRequest().build();
        }

        return service.updateLabCapacity(courseId, newCapacity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{courseId}/professors/{professorId}")
    public ResponseEntity<Course> assignProfessor(@PathVariable Long courseId,
                                                  @PathVariable Long professorId,
                                                  Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        return service.assignProfessorToCourse(courseId, professorId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{courseId}/professors/{professorId}")
    public ResponseEntity<Course> unassignProfessor(@PathVariable Long courseId,
                                                    @PathVariable Long professorId,
                                                    Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        return service.removeProfessorFromCourse(courseId, professorId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/timeslots")
    public ResponseEntity<List<Map<String, String>>> getTimeSlots() {
        return ResponseEntity.ok(excelScheduleService.getTimeSlots());
    }

    @GetMapping("/teacher-ids")
    public ResponseEntity<List<Long>> getTeacherIds() {
        return ResponseEntity.ok(service.getAllGroupTeacherIds());
    }
}