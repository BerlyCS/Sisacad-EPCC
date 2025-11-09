package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.service.DemoUserService;
import com.application.sisacadepcc.service.EmailVerificationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final DemoUserService demoUserService;

    public AuthController(EmailVerificationService emailVerificationService, DemoUserService demoUserService) {
        this.emailVerificationService = emailVerificationService;
        this.demoUserService = demoUserService;
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

    @PostMapping("/demo-login")
    public ResponseEntity<Map<String, Object>> demoLogin(@RequestBody Map<String, String> request, HttpServletRequest servletRequest) {
        if (!demoUserService.isDemoLoginEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Demo login no está habilitado"));
        }

        String role = Optional.ofNullable(request.get("role"))
                .map(String::trim)
                .map(String::toUpperCase)
                .orElse("");

        Optional<DemoUserService.DemoUserProfile> profileOptional = demoUserService.findProfileByRole(role);
        if (profileOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Rol de demo no soportado"));
        }

        DemoUserService.DemoUserProfile profile = profileOptional.get();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", profile.email());
        attributes.put("name", profile.displayName());
        attributes.put("picture", profile.pictureUrl());

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        DefaultOAuth2User principal = new DefaultOAuth2User(authorities, attributes, "email");
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(principal, authorities, "demo");

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("authenticated", true);
        responseBody.put("name", profile.displayName());
        responseBody.put("email", profile.email());
        responseBody.put("role", profile.role());
        responseBody.put("isAdmin", "ADMIN".equals(profile.role()));
        responseBody.put("picture", profile.pictureUrl());

        if (profile.documentoIdentidad() != null) {
            responseBody.put("documentoIdentidad", profile.documentoIdentidad());
        }

        return ResponseEntity.ok(responseBody);
    }
}