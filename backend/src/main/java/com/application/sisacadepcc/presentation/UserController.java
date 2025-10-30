package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.domain.model.Student;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final StudentRepository studentRepository;

    public UserController(AuthorizationService authorizationService, StudentRepository studentRepository) {
        this.authorizationService = authorizationService;
        this.studentRepository = studentRepository;
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

            // Buscar información adicional si es estudiante
            String documentoIdentidad = null;
            if ("STUDENT".equals(role)) {
                try {
                    // Usar el repositorio directamente - NO llama al endpoint protegido
                    Optional<Student> student = studentRepository.findByCorreoInstitucional(email);
                    if (student.isPresent()) {
                        documentoIdentidad = student.get().getDocumentoIdentidad();
                    }
                } catch (Exception e) {
                    // Si hay error al buscar el estudiante, continuar sin documentoIdentidad
                    System.err.println("Error buscando estudiante por email: " + e.getMessage());
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

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
    }
}