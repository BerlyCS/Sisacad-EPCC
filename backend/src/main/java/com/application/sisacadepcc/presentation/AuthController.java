package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.service.EmailVerificationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
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
                    "oauthUrl", "/oauth2/authorization/google"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "El correo electrónico no está registrado en el sistema"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Invalidar la sesión
            SecurityContextHolder.clearContext();

            // Invalidar la sesión HTTP
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // Limpiar cookies de autenticación
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("JSESSIONID")) {
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                    }
                }
            }

            // Limpiar el contexto de seguridad
            new SecurityContextLogoutHandler().logout(request, response,
                    SecurityContextHolder.getContext().getAuthentication());

            return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error durante el logout"));
        }
    }
}