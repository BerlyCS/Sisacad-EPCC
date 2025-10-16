package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.service.EmailVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // Para desarrollo con Vue
public class AuthController {

    private final EmailVerificationService emailVerificationService;

    public AuthController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "message", "Email es requerido"));
        }

        boolean emailExists = emailVerificationService.verifyEmailExists(email.trim());

        if (emailExists) {
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Email verificado correctamente",
                    "oauthUrl", "/oauth2/authorization/google" // URL para OAuth2 de Google
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "El correo electrónico no está registrado en el sistema"
            ));
        }
    }
}