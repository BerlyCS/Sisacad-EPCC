package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.presentation.dto.ClassroomOptionResponse;
import com.application.sisacadepcc.presentation.dto.CreateLabSectionRequest;
import com.application.sisacadepcc.presentation.dto.LabSectionResponse;
import com.application.sisacadepcc.presentation.dto.LabSlotSuggestionResponse;
import com.application.sisacadepcc.presentation.dto.UpdateLabSectionRequest;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.SecretaryLabManagementService;
import com.application.sisacadepcc.service.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/secretary/labs")
public class SecretaryLabController {

    private final SecretaryLabManagementService labManagementService;
    private final AuthorizationService authorizationService;

    public SecretaryLabController(SecretaryLabManagementService labManagementService,
                                  AuthorizationService authorizationService) {
        this.labManagementService = labManagementService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/theory")
    public ResponseEntity<List<Course>> listTheoryCourses(Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(labManagementService.listTheoryCourses());
    }

    @GetMapping("/classrooms")
    public ResponseEntity<List<ClassroomOptionResponse>> listClassrooms(
            @RequestParam(name = "labOnly", defaultValue = "true") boolean labOnly,
            Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(labManagementService.listClassrooms(labOnly));
    }

    @GetMapping("/theory/{theoryCourseId}")
    public ResponseEntity<List<LabSectionResponse>> listLabSections(@PathVariable Long theoryCourseId,
                                                                    Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(labManagementService.listLabSections(theoryCourseId));
    }

    @GetMapping("/theory/{theoryCourseId}/suggested-slots")
    public ResponseEntity<List<LabSlotSuggestionResponse>> listSuggestedSlots(@PathVariable Long theoryCourseId,
                                                                              Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(labManagementService.getSlotSuggestions(theoryCourseId));
    }

    @PostMapping
    public ResponseEntity<?> createLabSection(@RequestBody CreateLabSectionRequest request,
                                              Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            LabSectionResponse response = labManagementService.createLabSection(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/{labCourseId}")
    public ResponseEntity<?> updateLabSection(@PathVariable Long labCourseId,
                                              @RequestBody UpdateLabSectionRequest request,
                                              Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            LabSectionResponse response = labManagementService.updateLabSection(labCourseId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @DeleteMapping("/{labCourseId}")
    public ResponseEntity<?> deleteLabSection(@PathVariable Long labCourseId,
                                              Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            labManagementService.deleteLabSection(labCourseId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
