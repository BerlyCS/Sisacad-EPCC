package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.presentation.dto.CourseSummaryResponse;
import com.application.sisacadepcc.presentation.dto.CourseDetailsResponse;
import com.application.sisacadepcc.presentation.dto.CourseGroupAssignmentResponse;
import com.application.sisacadepcc.presentation.dto.CourseStudentResponse;
import com.application.sisacadepcc.presentation.dto.CreateCourseGroupRequest;
import com.application.sisacadepcc.presentation.dto.UpdateCapacityRequest;
import com.application.sisacadepcc.domain.repository.EnrollmentRepository;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.CourseService;
import com.application.sisacadepcc.service.UserRole;
import com.application.sisacadepcc.service.dto.CourseImportResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;
    private final AuthorizationService authorizationService;
    private final EnrollmentRepository enrollmentRepository;

    public CourseController(CourseService service,
                            AuthorizationService authorizationService,
                            EnrollmentRepository enrollmentRepository) {
        this.service = service;
        this.authorizationService = authorizationService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping
    public ResponseEntity<List<CourseSummaryResponse>> getAllCourses() {
        var enrollmentCounts = service.getEnrollmentCountsByCourse();
        var list = service.getAllCourses().stream()
            .map(course -> CourseSummaryResponse.from(course, enrollmentCounts.getOrDefault(course.getCourseId(), 0L)))
            .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{courseId}/groups")
    public ResponseEntity<List<CourseGroupAssignmentResponse>> getCourseGroups(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                service.getCourseGroups(courseId).stream()
                        .map(CourseGroupAssignmentResponse::from)
                        .toList()
        );
    }

    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<CourseStudentResponse>> getCourseStudents(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                service.getStudentsByCourse(courseId).stream()
                        .map(CourseStudentResponse::from)
                        .toList()
        );
    }

    @PostMapping("/{courseId}/groups")
    public ResponseEntity<CourseGroupAssignmentResponse> createCourseGroup(@PathVariable Long courseId,
                                                                          @RequestBody CreateCourseGroupRequest request,
                                                                          Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        try {
            return service.createCourseGroup(courseId, request)
                    .map(CourseGroupAssignmentResponse::from)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailsResponse> getCourseDetails(@PathVariable Long id) {
        return service.getCourseDetails(id)
                .map(CourseDetailsResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course, Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        Course created = service.createCourse(course);
        return ResponseEntity.ok(created);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseImportResult> importCourses(@RequestPart("file") MultipartFile file,
                                                            Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        try {
            return ResponseEntity.ok(service.importCourses(file));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new CourseImportResult(List.of(), List.of(ex.getMessage()), 0, 0));
        }
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

    @PatchMapping("/groups/{groupId}/capacity")
    public ResponseEntity<CourseGroup> updateGroupCapacity(@PathVariable Long groupId,
                                                           @RequestBody UpdateCapacityRequest request,
                                                           Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        Integer newCapacity = request != null ? request.capacity() : null;
        if (newCapacity != null && newCapacity < 0) {
            return ResponseEntity.badRequest().build();
        }

        return service.updateGroupCapacity(groupId, newCapacity)
                .map(group -> {
                    // Check if over-enrolled
                    int enrolled = (int) enrollmentRepository.countByCourseGroupId(group.getId());
                    if (newCapacity != null && enrolled > newCapacity) {
                        // Still proceed, but add a warning header
                        return ResponseEntity.ok()
                                .header("X-Warning", "Group is over-enrolled: " + enrolled + " students for capacity " + newCapacity)
                                .body(group);
                    }
                    return ResponseEntity.ok(group);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{courseId}/professors/{professorId}")
    public ResponseEntity<CourseGroupAssignmentResponse> assignProfessor(@PathVariable Long courseId,
                                                                        @PathVariable Long professorId,
                                                                        @RequestParam("groupId") Long groupId,
                                                                        Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        return service.assignProfessorToGroup(courseId, groupId, professorId)
                .map(CourseGroupAssignmentResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{courseId}/professors/{professorId}")
    public ResponseEntity<CourseGroupAssignmentResponse> unassignProfessor(@PathVariable Long courseId,
                                                                          @PathVariable Long professorId,
                                                                          @RequestParam("groupId") Long groupId,
                                                                          Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }

        return service.removeProfessorFromGroup(courseId, groupId, professorId)
                .map(CourseGroupAssignmentResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/timeslots")
    public ResponseEntity<List<Map<String, String>>> getTimeSlots() {
        // Excel parser disabled, return static time slots
        List<Map<String, String>> timeSlots = List.of(
                Map.of("startTime", "07:00", "endTime", "07:50"),
                Map.of("startTime", "07:50", "endTime", "08:40"),
                Map.of("startTime", "08:50", "endTime", "09:40"),
                Map.of("startTime", "09:40", "endTime", "10:30"),
                Map.of("startTime", "10:40", "endTime", "11:30"),
                Map.of("startTime", "11:30", "endTime", "12:20"),
                Map.of("startTime", "12:20", "endTime", "13:10"),
                Map.of("startTime", "13:10", "endTime", "14:00"),
                Map.of("startTime", "14:00", "endTime", "14:50"),
                Map.of("startTime", "14:50", "endTime", "15:40"),
                Map.of("startTime", "15:50", "endTime", "16:40"),
                Map.of("startTime", "16:40", "endTime", "17:30"),
                Map.of("startTime", "17:40", "endTime", "18:30"),
                Map.of("startTime", "18:30", "endTime", "19:20"),
                Map.of("startTime", "19:20", "endTime", "20:10")
        );
        return ResponseEntity.ok(timeSlots);
    }

    @GetMapping("/teacher-ids")
    public ResponseEntity<List<Long>> getTeacherIds() {
        return ResponseEntity.ok(service.getAllGroupTeacherIds());
    }
}