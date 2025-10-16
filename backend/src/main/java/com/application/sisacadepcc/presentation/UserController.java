package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.service.AuthorizationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final AuthorizationService authorizationService;

    public UserController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.ok(Map.of("authenticated", false));
            }

            // Verificar que los atributos esenciales existan
            String email = principal.getAttribute("email");
            if (email == null) {
                return ResponseEntity.ok(Map.of("authenticated", false));
            }

            String role = authorizationService.getUserRole(
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
            );

            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "name", principal.getAttribute("name") != null ? principal.getAttribute("name") : "Usuario",
                    "email", email,
                    "picture", principal.getAttribute("picture"),
                    "role", role,
                    "isAdmin", "ADMIN".equals(role)
            ));
        } catch (Exception e) {
            // En caso de cualquier error, considerar como no autenticado
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
    }
}