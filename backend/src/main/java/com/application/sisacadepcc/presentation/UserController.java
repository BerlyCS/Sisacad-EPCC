package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.domain.model.Student;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String role = authorizationService.getUserRole(authentication);

            // Buscar información adicional si es estudiante
            String documentoIdentidad = null;
            String cui = null;
            if ("STUDENT".equals(role)) {
                try {
                    Optional<Student> student = authorizationService.getAuthenticatedStudent(authentication);
                    if (student.isPresent()) {
                        documentoIdentidad = student.get().getDocumentoIdentidad();
                        cui = student.get().getCui();
                    } else if (principal instanceof OAuth2User oauth2User) {
                        documentoIdentidad = oauth2User.getAttribute("documentoIdentidad");
                        cui = oauth2User.getAttribute("cui");
                    }
                } catch (Exception e) {
                    System.err.println("Error buscando estudiante autenticado: " + e.getMessage());
                }
            }

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("name", principal.getAttribute("name") != null ? principal.getAttribute("name") : "Usuario");
            response.put("email", email);
            response.put("picture", principal.getAttribute("picture"));
            response.put("role", role);
            response.put("isAdmin", "ADMIN".equals(role));

            if (documentoIdentidad != null) {
                response.put("documentoIdentidad", documentoIdentidad);
            }

            if (cui != null) {
                response.put("cui", cui);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
    }
}