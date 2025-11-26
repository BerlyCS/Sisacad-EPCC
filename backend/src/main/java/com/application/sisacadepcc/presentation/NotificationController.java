package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.infrastructure.repository.jpa.NotificationEntity;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;

    public NotificationController(NotificationService notificationService, AuthorizationService authorizationService) {
        this.notificationService = notificationService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationEntity>> getMyNotifications(Authentication authentication) {
        Long userId = authorizationService.getAuthenticatedUserId(authentication);
        if (userId == null)
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
